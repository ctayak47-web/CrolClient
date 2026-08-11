"""
Быстрый и безопасный inline-модуль regdateid_bot.

Главное правило inline-режима:
Telegram ожидает ответ очень быстро, поэтому здесь нет сетевых запросов,
файлового I/O, SQLite и других потенциально блокирующих операций.
Анализ выполняется только над локальными данными и защищён коротким deadline.
"""

import logging
import time
from datetime import datetime
from threading import RLock
from typing import Dict, List, Optional, Tuple

from .analyzer import MILESTONES, RegistrationAnalyzer

logger = logging.getLogger(__name__)

# В продакшене лучше не тратить весь Telegram timeout на Python-код.
INLINE_DEADLINE_SECONDS = 1.0

# Длина Telegram ID ограничивается здравым пределом.
MIN_TELEGRAM_ID = 1
MAX_TELEGRAM_ID = 20_000_000_000

EMOJI_ID = "5260399854500191689"
EMOJI_DATE = "5256143829672672750"
EMOJI_AGE = "5258105663359294787"
EMOJI_ACCURACY = "6032742198179532882"
EMOJI_BOT = "5258509201306557640"
EMOJI_DOWNLOAD = "5400176691815406212"
EMOJI_OPEN = "5328189264658184861"
EMOJI_HELP = "5258509201306557640"
EMOJI_ERROR = "6032742198179532882"


class InlineCache:
    """Небольшой потокобезопасный TTL-кэш."""

    def __init__(self, ttl_seconds: int = 86400):
        self.cache: Dict[int, Tuple[dict, float]] = {}
        self.ttl = max(1, int(ttl_seconds))
        self._lock = RLock()

    def get(self, user_id: int) -> Optional[dict]:
        now = time.monotonic()
        with self._lock:
            item = self.cache.get(user_id)
            if item is None:
                return None

            data, created = item
            if now - created >= self.ttl:
                self.cache.pop(user_id, None)
                return None

            return data

    def set(self, user_id: int, data: dict) -> None:
        with self._lock:
            self.cache[user_id] = (data, time.monotonic())

    def clear_old(self) -> None:
        now = time.monotonic()
        with self._lock:
            expired = [
                uid
                for uid, (_, created) in self.cache.items()
                if now - created >= self.ttl
            ]
            for uid in expired:
                self.cache.pop(uid, None)


class InlineHandler:
    """Локальный анализатор для inline-запросов."""

    def __init__(self):
        self.analyzer = RegistrationAnalyzer(MILESTONES)
        self.cache = InlineCache(ttl_seconds=86400)

    @staticmethod
    def parse_query(query: str) -> Optional[int]:
        """
        Мгновенно принимает только числовой Telegram ID.
        Никаких API-запросов по username здесь нет.
        """
        if not isinstance(query, str):
            return None

        value = query.strip()
        if not value or len(value) > 24:
            return None

        # Telegram user ID положительный. Отрицательные chat IDs не анализируем.
        if not value.isdigit():
            return None

        try:
            user_id = int(value)
        except (TypeError, ValueError, OverflowError):
            return None

        if user_id < MIN_TELEGRAM_ID or user_id > MAX_TELEGRAM_ID:
            return None

        return user_id

    def analyze_user(self, user_id: int, deadline: float) -> Dict:
        if time.monotonic() >= deadline:
            raise TimeoutError("inline analysis deadline exceeded")

        cached = self.cache.get(user_id)
        if cached is not None:
            return cached

        timestamp = self.analyzer.calculate_timestamp(user_id)

        if time.monotonic() >= deadline:
            raise TimeoutError("inline analysis deadline exceeded")

        reg_date = datetime.fromtimestamp(timestamp / 1000)
        years, months, days = self.analyzer.calculate_age(reg_date)
        precision = self.analyzer.get_precision(user_id)

        result = {
            "user_id": user_id,
            "timestamp": timestamp,
            "reg_date": reg_date.strftime("%d.%m.%Y %H:%M:%S"),
            "age": f"{years}л {months}м {days}д",
            "precision": precision,
            "years": years,
            "months": months,
            "days": days,
        }

        self.cache.set(user_id, result)
        return result

    @staticmethod
    def format_result_text(data: Dict) -> str:
        return (
            f'<b>├ <tg-emoji id="{EMOJI_ID}">👾</tg-emoji> ID:</b> '
            f'<code>{data["user_id"]}</code>\n'
            f'<b>├ <tg-emoji id="{EMOJI_DATE}">👾</tg-emoji> Регистрация:</b> '
            f'<code>{data["reg_date"]}</code>\n'
            f'<b>├ <tg-emoji id="{EMOJI_AGE}">👾</tg-emoji> Возраст:</b> '
            f'<code>{data["age"]}</code>\n'
            f'<b>└ <tg-emoji id="{EMOJI_ACCURACY}">⚙️</tg-emoji> Точность:</b> '
            f'<i>{data["precision"]}</i>\n\n'
            f'<b><tg-emoji id="{EMOJI_BOT}">📍</tg-emoji> Юзернейм бота:</b> '
            f'<code>@regdateid_bot</code>'
        )

    @staticmethod
    def format_result_description(data: Dict) -> str:
        return (
            f'ID {data["user_id"]} | {data["reg_date"]} | '
            f'{data["precision"]}'
        )


def _safe_error_text(message: str) -> str:
    # Ошибки здесь фиксированные, поэтому HTML-инъекции исключены.
    return (
        f'<tg-emoji id="{EMOJI_ERROR}">⚠️</tg-emoji> '
        f'<b>{message}</b>'
    )


def build_inline_results(
    inline_handler: InlineHandler,
    query: str,
) -> Tuple[List[Dict], Optional[str]]:
    """
    ВСЕГДА возвращает (results, error).

    Функция намеренно не делает:
    - HTTP-запросов;
    - SQLite-запросов;
    - чтения файлов;
    - sleep;
    - retry.

    При любой проблеме возвращается пустой список и короткая ошибка.
    """
    results: List[Dict] = []
    error: Optional[str] = None
    started = time.monotonic()
    deadline = started + INLINE_DEADLINE_SECONDS

    try:
        user_id = InlineHandler.parse_query(query)

        if user_id is None:
            if isinstance(query, str) and query.strip():
                return [], (
                    f'<tg-emoji id="{EMOJI_ERROR}">⚠️</tg-emoji> '
                    f'<b>Введите числовой Telegram ID.</b>'
                )
            return [], None

        if time.monotonic() >= deadline:
            return [], _safe_error_text("Запрос занял слишком много времени.")

        data = inline_handler.analyze_user(user_id, deadline)

        if time.monotonic() >= deadline:
            return [], _safe_error_text("Запрос занял слишком много времени.")

        result = {
            "type": "article",
            "id": f"reg_{user_id}",
            "title": f"ID {user_id}",
            "description": inline_handler.format_result_description(data),
            "input_message_content": {
                "message_text": inline_handler.format_result_text(data),
                "parse_mode": "HTML",
            },
            "reply_markup": {
                "inline_keyboard": [
                    [
                        {
                            "text": (
                                f'<tg-emoji id="{EMOJI_DOWNLOAD}">'
                                '📄</tg-emoji> Скачать TXT отчёт'
                            ),
                            "url": (
                                "https://t.me/regdateid_bot"
                                f"?start=download_txt_{user_id}"
                            ),
                            "style": "primary",
                        },
                        {
                            "text": (
                                f'<tg-emoji id="{EMOJI_DOWNLOAD}">'
                                '🌐</tg-emoji> Скачать HTML отчёт'
                            ),
                            "url": (
                                "https://t.me/regdateid_bot"
                                f"?start=download_html_{user_id}"
                            ),
                            "style": "primary",
                        },
                    ],
                    [
                        {
                            "text": (
                                f'<tg-emoji id="{EMOJI_OPEN}">'
                                '🚀</tg-emoji> Перейти в бота'
                            ),
                            "url": "https://t.me/regdateid_bot",
                            "style": "success",
                        }
                    ],
                ],
            },
        }
        results.append(result)
        return results, None

    except TimeoutError:
        return [], _safe_error_text("Запрос занял слишком много времени.")
    except (ValueError, OverflowError, TypeError):
        logger.exception("inline validation/analyze error")
        return [], _safe_error_text("Некорректный ID.")
    except Exception:
        # Inline-обработчик никогда не должен уронить polling.
        logger.exception("unexpected inline error")
        return [], _safe_error_text("Не удалось обработать запрос.")
