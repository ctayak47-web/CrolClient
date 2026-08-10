import logging
import os
import time
from datetime import datetime
from typing import Dict, Optional, Tuple

import requests

from . import storage
from .analyzer import RegistrationAnalyzer, MILESTONES
from .templates import generate_txt_report, generate_html_report

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

TOKEN = os.environ.get("BOT_TOKEN_DATA", "")
ADMIN_ID = int(os.environ.get("ADMIN_ID_DATA", "0") or 0)

REPORTS_DIR = os.environ.get("REGBOT_REPORTS_DIR", "data/reports")

INSTRUCTIONS_TEXT = (
    "<b>как пользоваться ботом</b>\n\n"
    "1. «свой id + регистрация» — покажет анализ вашего собственного аккаунта "
    "и сохранит вас в базе.\n"
    "2. «по id» — введите числовой telegram id любого аккаунта, бот покажет "
    "оценочную дату регистрации.\n"
    "3. «переслать сообщение» — перешлите боту сообщение нужного пользователя, "
    "и id подставится автоматически.\n"
    "4. команда <code>/reg1 &lt;id&gt;</code> — быстрый разбор без меню, "
    "например: <code>/reg1 123456789</code>.\n"
    "5. также можно просто прислать число (id) в чат — бот распознает его "
    "и сразу выдаст результат.\n\n"
    "после расчёта результат остаётся в чате, а кнопками ниже можно скачать "
    "его в виде .txt или .html файла.\n\n"
    "<i>точность расчёта: «эталонная» — id совпадает с опорной точкой, "
    "«интерполяция» — расчёт между двумя опорными точками, «экстраполяция» — "
    "прогноз за пределами последних известных данных.</i>"
)


class TelegramBot:
    def __init__(self, token: str, admin_id: int):
        self.token = token
        self.base_url = f"https://api.telegram.org/bot{token}"
        self.admin_id = admin_id
        self.analyzer = RegistrationAnalyzer(MILESTONES)
        self.user_states: Dict[int, Optional[str]] = {}
        self.user_data: Dict[int, dict] = {}
        self.user_messages: Dict[int, int] = {}
        self.offset = 0
        os.makedirs(REPORTS_DIR, exist_ok=True)

    # ─── low level http ──────────────────────────────────────────────────

    def _make_request(self, method: str, params: Dict = None, files: Dict = None, retry_count: int = 3) -> Optional[Dict]:
        for attempt in range(retry_count):
            try:
                url = f"{self.base_url}/{method}"
                if files:
                    response = requests.post(url, data=params, files=files, timeout=15)
                else:
                    response = requests.post(url, json=params, timeout=15)

                if response.status_code == 429:
                    retry_after = response.json().get('parameters', {}).get('retry_after', 5)
                    logger.warning(f"rate limit, waiting {retry_after}s")
                    time.sleep(retry_after)
                    continue

                if response.status_code == 403:
                    logger.error(f"access denied: {method}")
                    return None

                return response.json()

            except requests.exceptions.Timeout:
                logger.warning(f"timeout attempt {attempt + 1}/{retry_count}")
                if attempt < retry_count - 1:
                    time.sleep(2)
                continue
            except Exception as e:
                logger.error(f"api error: {e}")
                if attempt < retry_count - 1:
                    time.sleep(2)
                continue

        return None

    def send_message(self, chat_id: int, text: str, reply_markup: Dict = None) -> Optional[Dict]:
        params = {"chat_id": chat_id, "text": text, "parse_mode": "HTML"}
        if reply_markup:
            params["reply_markup"] = reply_markup
        return self._make_request("sendMessage", params)

    def edit_message(self, chat_id: int, message_id: int, text: str, reply_markup: Dict = None) -> Optional[Dict]:
        params = {"chat_id": chat_id, "message_id": message_id, "text": text, "parse_mode": "HTML"}
        if reply_markup:
            params["reply_markup"] = reply_markup
        return self._make_request("editMessageText", params)

    def delete_message(self, chat_id: int, message_id: int) -> Optional[Dict]:
        return self._make_request("deleteMessage", {"chat_id": chat_id, "message_id": message_id})

    def answer_callback(self, callback_id: str, text: str = None) -> Optional[Dict]:
        params = {"callback_query_id": callback_id}
        if text:
            params["text"] = text
        return self._make_request("answerCallbackQuery", params)

    # ─── keyboards ───────────────────────────────────────────────────────

    def get_main_keyboard(self, is_admin: bool = False) -> Dict:
        keyboard = [
            [{"text": "свой id + регистрация", "callback_data": "my_id_reg"}],
            [{"text": "по id", "callback_data": "method_id"}],
            [{"text": "переслать сообщение", "callback_data": "method_forward"}],
            [{"text": "📖 инструкция", "callback_data": "instructions"}],
        ]

        if is_admin:
            keyboard.append([{"text": "админ-панель", "callback_data": "admin_panel"}])

        return {"inline_keyboard": keyboard}

    def get_result_keyboard(self) -> Dict:
        return {
            "inline_keyboard": [
                [{"text": "скачать txt", "callback_data": "download_txt"}, {"text": "скачать html", "callback_data": "download_html"}],
                [{"text": "назад", "callback_data": "back"}]
            ]
        }

    def get_admin_keyboard(self) -> Dict:
        return {
            "inline_keyboard": [
                [{"text": "статистика", "callback_data": "admin_stats"}],
                [{"text": "рассылка", "callback_data": "admin_broadcast"}],
                [{"text": "назад", "callback_data": "back"}]
            ]
        }

    def get_back_keyboard(self) -> Dict:
        return {"inline_keyboard": [[{"text": "назад", "callback_data": "back"}]]}

    # ─── stats / broadcast ───────────────────────────────────────────────

    def register_user(self, user_id: int, username: str = None, reg_date: str = None, timestamp: int = None):
        try:
            storage.register_user(user_id, username, reg_date, timestamp)
        except Exception as e:
            logger.error(f"db error: {e}")

    def get_stats(self) -> int:
        try:
            return storage.get_stats()
        except Exception as e:
            logger.error(f"stats error: {e}")
            return 0

    def broadcast(self, message: str) -> int:
        try:
            users = storage.all_user_ids()
            count = 0
            for user_id in users:
                try:
                    self.send_message(user_id, message)
                    count += 1
                    time.sleep(0.05)
                except Exception as e:
                    logger.error(f"broadcast error: {e}")
            return count
        except Exception as e:
            logger.error(f"broadcast error: {e}")
            return 0

    # ─── analysis ────────────────────────────────────────────────────────

    def analyze_and_display(self, chat_id: int, target_id: int, username: str = None) -> Tuple[str, datetime, int]:
        timestamp = self.analyzer.calculate_timestamp(target_id)
        reg_date = datetime.fromtimestamp(timestamp / 1000)
        years, months, days = self.analyzer.calculate_age(reg_date)
        precision = self.analyzer.get_precision(target_id)

        result_text = f"id: {target_id}\n"
        if username:
            result_text += f"username: @{username}\n"
        result_text += f"\nдата регистрации: {reg_date.strftime('%d.%m.%Y %H:%M:%S')}\n"
        result_text += f"возраст: {years} лет, {months} мес, {days} дн\n"
        result_text += f"точность: {precision}"

        return result_text, reg_date, timestamp

    def _store_result(self, chat_id: int, target_id: int, reg_date: datetime, timestamp: int, username: Optional[str]):
        self.user_data[chat_id] = {
            "result_text": None,
            "target_id": target_id,
            "reg_date": reg_date,
            "timestamp": timestamp,
            "username": username,
        }

    def _edit_or_send(self, chat_id: int, text: str, reply_markup: Dict = None):
        existing_msg_id = self.user_messages.get(chat_id)
        if existing_msg_id:
            resp = self.edit_message(chat_id, existing_msg_id, text, reply_markup)
            if resp and resp.get("ok"):
                return
        resp = self.send_message(chat_id, text, reply_markup)
        if resp and resp.get("result"):
            self.user_messages[chat_id] = resp["result"]["message_id"]

    def _deliver_result(self, chat_id: int, target_id: int, username: Optional[str] = None, edit: bool = True):
        """Считает и показывает результат, сохраняя его прямо в чате (не удаляя)."""
        result_text, reg_date, timestamp = self.analyze_and_display(chat_id, target_id, username)
        self._store_result(chat_id, target_id, reg_date, timestamp, username)

        existing_msg_id = self.user_messages.get(chat_id)
        if edit and existing_msg_id:
            resp = self.edit_message(chat_id, existing_msg_id, result_text, self.get_result_keyboard())
            if resp and resp.get("ok"):
                return
        # не удалось отредактировать (или не нужно) — отправляем новое сообщение,
        # оно остаётся в чате как обычное сообщение бота
        resp = self.send_message(chat_id, result_text, self.get_result_keyboard())
        if resp and resp.get("result"):
            self.user_messages[chat_id] = resp["result"]["message_id"]

    # ─── update loop ─────────────────────────────────────────────────────

    def process_updates(self):
        while True:
            try:
                params = {"offset": self.offset, "timeout": 30}
                response = self._make_request("getUpdates", params)

                if response and response.get("ok"):
                    for update in response.get("result", []):
                        try:
                            if "message" in update:
                                self.handle_message(update["message"])
                            elif "callback_query" in update:
                                self.handle_callback(update["callback_query"])
                        except Exception as e:
                            logger.error(f"update handling error: {e}")

                        self.offset = update["update_id"] + 1

                time.sleep(0.1)

            except Exception as e:
                logger.error(f"updates error: {e}")
                time.sleep(5)

    def handle_message(self, message: Dict):
        chat_id = message["chat"]["id"]
        user_id = message["from"]["id"]
        username = message["from"].get("username")

        text = message.get("text", "").strip() if "text" in message else None

        if text == "/start":
            self.delete_message(chat_id, message["message_id"])
            resp = self.send_message(chat_id, "выберите действие:",
                                       self.get_main_keyboard(user_id == self.admin_id))
            if resp and resp.get("result"):
                self.user_messages[chat_id] = resp["result"]["message_id"]
            self.user_states[chat_id] = None
            return

        if text and text.lower().startswith("/reg1"):
            parts = text.split(maxsplit=1)
            if len(parts) < 2 or not parts[1].strip().lstrip("-").isdigit():
                self.send_message(chat_id, "использование: /reg1 <id>\nнапример: /reg1 123456789")
                return
            target_id = int(parts[1].strip())
            self._deliver_result(chat_id, target_id, edit=False)
            return

        if text and chat_id in self.user_states and self.user_states[chat_id]:
            state = self.user_states[chat_id]
            self.delete_message(chat_id, message["message_id"])

            if state == "waiting_id":
                if text.lstrip("-").isdigit():
                    target_id = int(text)
                    self._deliver_result(chat_id, target_id, edit=True)
                    self.user_states[chat_id] = None
                else:
                    self._edit_or_send(chat_id, "ошибка: введите числовой id", self.get_back_keyboard())

            elif state == "waiting_broadcast":
                sent_count = self.broadcast(text)
                self._edit_or_send(chat_id, f"рассылка завершена\nотправлено: {sent_count} пользователей",
                                    self.get_back_keyboard())
                self.user_states[chat_id] = None
            return

        # удобство: если пользователь просто прислал число без всякого меню — считаем это id
        if text and text.lstrip("-").isdigit() and "forward_from" not in message:
            self._deliver_result(chat_id, int(text), edit=False)
            return

        if "forward_from" in message and self.user_states.get(chat_id) == "waiting_forward":
            forward_from = message["forward_from"]
            target_id = forward_from["id"]
            username_forward = forward_from.get("username")

            self.delete_message(chat_id, message["message_id"])
            self._deliver_result(chat_id, target_id, username_forward, edit=True)
            self.user_states[chat_id] = None

    def handle_callback(self, callback: Dict):
        chat_id = callback["message"]["chat"]["id"]
        message_id = callback["message"]["message_id"]
        data = callback["data"]
        callback_id = callback["id"]
        user_id = callback["from"]["id"]
        username = callback["from"].get("username")

        if data == "my_id_reg":
            target_id = chat_id
            result_text, reg_date, timestamp = self.analyze_and_display(chat_id, target_id, username)
            self.register_user(target_id, username, reg_date.strftime('%d.%m.%Y %H:%M:%S'), timestamp)
            self._store_result(chat_id, target_id, reg_date, timestamp, username)
            self.edit_message(chat_id, message_id, result_text, self.get_result_keyboard())

        elif data == "method_id":
            self.edit_message(chat_id, message_id, "введите id аккаунта:", self.get_back_keyboard())
            self.user_states[chat_id] = "waiting_id"

        elif data == "method_forward":
            self.edit_message(chat_id, message_id, "перешлите сообщение от пользователя:", self.get_back_keyboard())
            self.user_states[chat_id] = "waiting_forward"

        elif data == "instructions":
            self.edit_message(chat_id, message_id, INSTRUCTIONS_TEXT, self.get_back_keyboard())

        elif data == "download_txt":
            self._send_report_file(chat_id, callback_id, "txt")

        elif data == "download_html":
            self._send_report_file(chat_id, callback_id, "html")

        elif data == "admin_panel":
            if user_id == self.admin_id:
                self.edit_message(chat_id, message_id, "админ-панель", self.get_admin_keyboard())
            else:
                self.answer_callback(callback_id, "доступ запрещен")

        elif data == "admin_stats":
            if user_id == self.admin_id:
                stats = self.get_stats()
                self.edit_message(chat_id, message_id, f"статистика\n\nпользователей: {stats}", self.get_back_keyboard())
            else:
                self.answer_callback(callback_id, "доступ запрещен")

        elif data == "admin_broadcast":
            if user_id == self.admin_id:
                self.edit_message(chat_id, message_id, "введите текст рассылки:", self.get_back_keyboard())
                self.user_states[chat_id] = "waiting_broadcast"
            else:
                self.answer_callback(callback_id, "доступ запрещен")

        elif data == "back":
            self.edit_message(chat_id, message_id, "выберите действие:",
                               self.get_main_keyboard(user_id == self.admin_id))
            self.user_states[chat_id] = None

        self.answer_callback(callback_id)

    def _send_report_file(self, chat_id: int, callback_id: str, kind: str):
        data = self.user_data.get(chat_id)
        if not data or "target_id" not in data:
            self.answer_callback(callback_id, "нет данных")
            return

        target_id = data["target_id"]
        reg_date = data["reg_date"]
        timestamp = data["timestamp"]
        username = data.get("username")
        years, months, days = self.analyzer.calculate_age(reg_date)
        precision = self.analyzer.get_precision(target_id)

        if kind == "txt":
            content = generate_txt_report(target_id, reg_date, timestamp, years, months, days,
                                           precision, len(MILESTONES), username)
            ext = "txt"
        else:
            content = generate_html_report(target_id, reg_date, timestamp, years, months, days,
                                            precision, len(MILESTONES), username)
            ext = "html"

        filename = f"{REPORTS_DIR}/report_{target_id}_{int(time.time())}.{ext}"

        try:
            with open(filename, 'w', encoding='utf-8') as f:
                f.write(content)

            with open(filename, 'rb') as f:
                self._make_request("sendDocument",
                                    {"chat_id": chat_id},
                                    {"document": (f"report_{target_id}.{ext}", f)})

            self.answer_callback(callback_id, f"{ext} отчет отправлен")

        except Exception as e:
            logger.error(f"download error: {e}")
            self.answer_callback(callback_id, "ошибка")

        finally:
            try:
                if os.path.exists(filename):
                    os.remove(filename)
            except Exception:
                pass


def run():
    if not TOKEN:
        print("[regbot] BOT_TOKEN_DATA не задан — бот не запущен")
        return
    print("[regbot] бот запущен, начинаю polling")
    bot = TelegramBot(TOKEN, ADMIN_ID)
    bot.process_updates()
