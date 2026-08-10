"""
Модуль инлайн-режима для regdatabot.

Обрабатывает инлайн-запросы вида: @юзбота 123456789 или @юзбота @username

Инлайн-режим позволяет пользователям вводить запрос о регистрации прямо в поле ввода,
видеть результаты в виде карточек и мгновенно вставить результат в чат.

Результаты кэшируются на 1 день, чтобы не пересчитывать одно и то же постоянно.
"""

import logging
import time
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Tuple

from .analyzer import RegistrationAnalyzer, MILESTONES

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


class InlineCache:
    """Простой кэш для результатов инлайн-запросов."""
    
    def __init__(self, ttl_seconds: int = 86400):
        self.cache: Dict[int, Tuple[dict, float]] = {}
        self.ttl = ttl_seconds
    
    def get(self, user_id: int) -> Optional[dict]:
        """Получить результат из кэша, если он ещё свежий."""
        if user_id not in self.cache:
            return None
        
        data, timestamp = self.cache[user_id]
        if time.time() - timestamp > self.ttl:
            del self.cache[user_id]
            return None
        
        return data
    
    def set(self, user_id: int, data: dict):
        """Сохранить результат в кэш."""
        self.cache[user_id] = (data, time.time())
    
    def clear_old(self):
        """Удалить старые записи (запускается периодически)."""
        current_time = time.time()
        expired = [uid for uid, (_, ts) in self.cache.items() 
                   if current_time - ts > self.ttl]
        for uid in expired:
            del self.cache[uid]


class InlineHandler:
    """Обработчик инлайн-запросов для regdatabot."""
    
    def __init__(self):
        self.analyzer = RegistrationAnalyzer(MILESTONES)
        self.cache = InlineCache(ttl_seconds=86400)  # 24 часа
    
    def parse_query(self, query: str) -> Optional[int]:
        """
        Парсит инлайн-запрос.
        
        Поддерживаются форматы:
        - 123456789 (числовой ID)
        - @username (если это бот, пытается извлечь ID)
        - username (без @)
        
        Возвращает числовой ID или None.
        """
        query = query.strip()
        
        if not query:
            return None
        
        # Если это просто число — используем как ID
        if query.lstrip('-').isdigit():
            return int(query)
        
        # Если это @username или просто username — пока мы не можем получить ID
        # (нужен доступ к User API Telegram, который требует auth)
        # Возвращаем None, сигнализируя об ошибке
        return None
    
    def analyze_user(self, user_id: int) -> Dict:
        """Анализирует аккаунт пользователя и возвращает результат."""
        # Проверяем кэш
        cached = self.cache.get(user_id)
        if cached:
            return cached
        
        # Вычисляем дату регистрации
        timestamp = self.analyzer.calculate_timestamp(user_id)
        reg_date = datetime.fromtimestamp(timestamp / 1000)
        years, months, days = self.analyzer.calculate_age(reg_date)
        precision = self.analyzer.get_precision(user_id)
        
        result = {
            "user_id": user_id,
            "timestamp": timestamp,
            "reg_date": reg_date.strftime('%d.%m.%Y %H:%M:%S'),
            "age": f"{years}л {months}м {days}д",
            "precision": precision,
            "years": years,
            "months": months,
            "days": days,
        }
        
        # Кэшируем результат
        self.cache.set(user_id, result)
        
        return result
    
    def format_result_text(self, data: Dict) -> str:
        """Форматирует результат анализа в текст для инлайн-карточки."""
        precision_emoji = {
            "эталонная точность": "✅",
            "интерполяция": "📊",
            "экстраполяция": "📈"
        }
        emoji = precision_emoji.get(data["precision"], "ℹ️")
        
        return (
            f"<b>ID:</b> {data['user_id']}\n"
            f"<b>Дата регистрации:</b> {data['reg_date']}\n"
            f"<b>Возраст:</b> {data['age']}\n"
            f"<b>Точность:</b> {emoji} {data['precision']}"
        )
    
    def format_result_description(self, data: Dict) -> str:
        """Краткое описание для превью инлайн-карточки."""
        return f"ID {data['user_id']} | {data['reg_date']} | {data['precision']}"


def build_inline_results(inline_handler: InlineHandler, query: str) -> Tuple[List[Dict], str]:
    """
    Собирает результаты инлайн-запроса для отправки в Telegram.
    
    Возвращает:
    - Список результатов для InlineQueryResultArticle
    - Текст ошибки (если есть)
    """
    results = []
    error = None
    
    # Парсим запрос
    user_id = inline_handler.parse_query(query)
    
    if user_id is None:
        if query:
            error = "формат: @regdatrobot 123456789 или @regdatrobot <числовой_id>"
        return results, error
    
    # Анализируем пользователя
    try:
        data = inline_handler.analyze_user(user_id)
        
        result = {
            "type": "article",
            "id": str(user_id),
            "title": f"Анализ аккаунта {user_id}",
            "description": inline_handler.format_result_description(data),
            "input_message_content": {
                "message_text": inline_handler.format_result_text(data),
                "parse_mode": "HTML"
            },
            "reply_markup": {
                "inline_keyboard": [
                    [
                        {"text": "скачать txt", "url": f"https://t.me/regbot_main?start=download_txt_{user_id}"},
                        {"text": "скачать html", "url": f"https://t.me/regbot_main?start=download_html_{user_id}"},
                    ]
                ]
            }
        }
        
        results.append(result)
    
    except Exception as e:
        logger.error(f"inline analysis error: {e}")
        error = "ошибка при анализе аккаунта"
    
    return results, error
