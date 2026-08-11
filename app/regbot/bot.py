import logging
import os
import threading
import time
from datetime import datetime
from typing import Dict, Optional, Tuple

import requests

from . import storage
from .analyzer import MILESTONES, RegistrationAnalyzer
from .inline import InlineHandler, build_inline_results
from .templates import generate_html_report, generate_txt_report

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)

TOKEN = os.environ.get("BOT_TOKEN_DATA", "")
ADMIN_ID = int(os.environ.get("ADMIN_ID_DATA", "0") or 0)

REPORTS_DIR = os.environ.get("REGBOT_REPORTS_DIR", "data/reports")
MENU_IMAGE = os.environ.get("REGBOT_MENU_IMAGE", "foto.png")

BOT_USERNAME = os.environ.get("REGBOT_USERNAME", "regdateid_bot")

EMOJI_ID = "5260399854500191689"
EMOJI_DATE = "5256143829672672750"
EMOJI_AGE = "5258105663359294787"
EMOJI_ACCURACY = "6032742198179532882"
EMOJI_BOT = "5258509201306557640"
EMOJI_MENU = "5328189264658184861"
EMOJI_BACK = "5258509201306557640"
EMOJI_INFO = "5258509201306557640"
EMOJI_DOWNLOAD = "5400176691815406212"

INSTRUCTIONS_TEXT = (
    f'<b><tg-emoji id="{EMOJI_INFO}">📖</tg-emoji> Как пользоваться ботом</b>\n\n'
    f'<tg-emoji id="{EMOJI_ID}">👾</tg-emoji> <b>Свой ID + регистрация</b> — '
    "анализ вашего аккаунта и сохранение результата.\n\n"
    f'<tg-emoji id="{EMOJI_ID}">👾</tg-emoji> <b>По ID</b> — '
    "введите числовой Telegram ID.\n\n"
    f'<tg-emoji id="{EMOJI_ID}">👾</tg-emoji> <b>Переслать сообщение</b> — '
    "перешлите сообщение пользователя, чтобы получить его ID.\n\n"
    f'<tg-emoji id="{EMOJI_ID}">👾</tg-emoji> <b>/reg1 &lt;id&gt;</b> — '
    "быстрый анализ.\n\n"
    f'<tg-emoji id="{EMOJI_ID}">👾</tg-emoji> Можно также просто отправить '
    "числовой ID.\n\n"
    f'<tg-emoji id="{EMOJI_ACCURACY}">⚙️</tg-emoji> '
    "Результаты не удаляются из чата."
)


class TelegramBot:
    def __init__(self, token: str, admin_id: int):
        self.token = token
        self.base_url = f"https://api.telegram.org/bot{token}"
        self.admin_id = admin_id

        self.analyzer = RegistrationAnalyzer(MILESTONES)
        self.inline_handler = InlineHandler()

        self.user_states: Dict[int, Optional[str]] = {}
        self.user_data: Dict[int, dict] = {}

        # Последнее сообщение-меню с фото. Результаты сюда НЕ подменяются:
        # они отправляются отдельными сообщениями и остаются в истории.
        self.menu_messages: Dict[int, int] = {}

        self.offset = 0
        self._request_lock = threading.RLock()

        os.makedirs(REPORTS_DIR, exist_ok=True)

    # ────────────────────────────────────────────────────────────────────
    # HTTP
    # ────────────────────────────────────────────────────────────────────

    def _make_request(
        self,
        method: str,
        params: Optional[Dict] = None,
        files: Optional[Dict] = None,
        retry_count: int = 3,
        timeout: Tuple[float, float] = (3.0, 15.0),
    ) -> Optional[Dict]:
        for attempt in range(max(1, retry_count)):
            try:
                url = f"{self.base_url}/{method}"

                if files:
                    response = requests.post(
                        url,
                        data=params or {},
                        files=files,
                        timeout=timeout,
                    )
                else:
                    response = requests.post(
                        url,
                        json=params or {},
                        timeout=timeout,
                    )

                if response.status_code == 429:
                    try:
                        retry_after = int(
                            response.json()
                            .get("parameters", {})
                            .get("retry_after", 1)
                        )
                    except Exception:
                        retry_after = 1

                    if attempt + 1 >= retry_count:
                        return None

                    time.sleep(min(retry_after, 3))
                    continue

                if response.status_code == 403:
                    logger.warning("Telegram denied request: %s", method)
                    return None

                if not response.ok:
                    logger.warning(
                        "Telegram HTTP %s for %s: %s",
                        response.status_code,
                        method,
                        response.text[:300],
                    )
                    return None

                try:
                    return response.json()
                except ValueError:
                    logger.warning("Invalid JSON from Telegram: %s", method)
                    return None

            except requests.exceptions.Timeout:
                logger.warning(
                    "Telegram timeout: %s attempt %s/%s",
                    method,
                    attempt + 1,
                    retry_count,
                )
                if attempt + 1 < retry_count:
                    time.sleep(0.2)
            except requests.RequestException as exc:
                logger.warning("Telegram request error %s: %s", method, exc)
                if attempt + 1 < retry_count:
                    time.sleep(0.2)
            except Exception:
                logger.exception("Unexpected API error in %s", method)
                break

        return None

    def send_message(
        self,
        chat_id: int,
        text: str,
        reply_markup: Optional[Dict] = None,
    ) -> Optional[Dict]:
        params = {
            "chat_id": chat_id,
            "text": text,
            "parse_mode": "HTML",
        }
        if reply_markup is not None:
            params["reply_markup"] = reply_markup
        return self._make_request("sendMessage", params)

    def edit_message(
        self,
        chat_id: int,
        message_id: int,
        text: str,
        reply_markup: Optional[Dict] = None,
    ) -> Optional[Dict]:
        params = {
            "chat_id": chat_id,
            "message_id": message_id,
            "text": text,
            "parse_mode": "HTML",
        }
        if reply_markup is not None:
            params["reply_markup"] = reply_markup
        return self._make_request("editMessageText", params)

    def send_photo(
        self,
        chat_id: int,
        caption: str,
        reply_markup: Optional[Dict] = None,
    ) -> Optional[Dict]:
        if not os.path.isfile(MENU_IMAGE):
            logger.warning("Menu image not found: %s", MENU_IMAGE)
            return self.send_message(chat_id, caption, reply_markup)

        params = {
            "chat_id": str(chat_id),
            "caption": caption,
            "parse_mode": "HTML",
        }
        if reply_markup is not None:
            # multipart data принимает JSON-строку.
            import json
            params["reply_markup"] = json.dumps(
                reply_markup,
                ensure_ascii=False,
                separators=(",", ":"),
            )

        try:
            with open(MENU_IMAGE, "rb") as photo:
                return self._make_request(
                    "sendPhoto",
                    params,
                    files={"photo": ("foto.png", photo, "image/png")},
                    retry_count=2,
                    timeout=(2.0, 15.0),
                )
        except OSError:
            logger.exception("Cannot open menu image")
            return self.send_message(chat_id, caption, reply_markup)
        except Exception:
            logger.exception("sendPhoto failed")
            return self.send_message(chat_id, caption, reply_markup)

    def edit_message_caption(
        self,
        chat_id: int,
        message_id: int,
        caption: str,
        reply_markup: Optional[Dict] = None,
    ) -> Optional[Dict]:
        params = {
            "chat_id": chat_id,
            "message_id": message_id,
            "caption": caption,
            "parse_mode": "HTML",
        }
        if reply_markup is not None:
            params["reply_markup"] = reply_markup
        return self._make_request("editMessageCaption", params)

    def delete_message(self, chat_id: int, message_id: int) -> Optional[Dict]:
        # Оставлено для совместимости, но пользовательские сообщения бот
        # больше НЕ удаляет.
        return self._make_request(
            "deleteMessage",
            {"chat_id": chat_id, "message_id": message_id},
        )

    def answer_callback(
        self,
        callback_id: str,
        text: Optional[str] = None,
    ) -> Optional[Dict]:
        params = {"callback_query_id": callback_id}
        if text:
            params["text"] = text
        return self._make_request(
            "answerCallbackQuery",
            params,
            retry_count=1,
            timeout=(1.0, 2.0),
        )

    def answer_inline_query(
        self,
        inline_query_id: str,
        results: list,
        cache_time: int = 5,
    ) -> Optional[Dict]:
        params = {
            "inline_query_id": inline_query_id,
            "results": results,
            "cache_time": cache_time,
            "is_personal": True,
        }
        # Inline ответ критичен по времени: только один короткий запрос.
        return self._make_request(
            "answerInlineQuery",
            params,
            retry_count=1,
            timeout=(0.5, 2.0),
        )

    # ────────────────────────────────────────────────────────────────────
    # KEYBOARDS
    # ────────────────────────────────────────────────────────────────────

    @staticmethod
    def _button(text: str, callback_data: str, style: str = "primary") -> Dict:
        return {
            "text": text,
            "callback_data": callback_data,
            "style": style,
        }

    def get_main_keyboard(self, is_admin: bool = False) -> Dict:
        keyboard = [
            [
                self._button(
                    f'<tg-emoji id="{EMOJI_ID}">👾</tg-emoji> Свой ID + регистрация',
                    "my_id_reg",
                    "primary",
                )
            ],
            [
                self._button(
                    f'<tg-emoji id="{EMOJI_ID}">🔎</tg-emoji> По ID',
                    "method_id",
                    "primary",
                )
            ],
            [
                self._button(
                    f'<tg-emoji id="{EMOJI_ID}">📨</tg-emoji> Переслать сообщение',
                    "method_forward",
                    "primary",
                )
            ],
            [
                self._button(
                    f'<tg-emoji id="{EMOJI_INFO}">📖</tg-emoji> Инструкция',
                    "instructions",
                    "primary",
                )
            ],
        ]

        if is_admin:
            keyboard.append(
                [
                    self._button(
                        f'<tg-emoji id="{EMOJI_MENU}">⚙️</tg-emoji> Админ-панель',
                        "admin_panel",
                        "danger",
                    )
                ]
            )

        return {"inline_keyboard": keyboard}

    def get_result_keyboard(self) -> Dict:
        return {
            "inline_keyboard": [
                [
                    {
                        "text": (
                            f'<tg-emoji id="{EMOJI_DOWNLOAD}">📄</tg-emoji> '
                            "Скачать TXT отчёт"
                        ),
                        "callback_data": "download_txt",
                        "style": "primary",
                    },
                    {
                        "text": (
                            f'<tg-emoji id="{EMOJI_DOWNLOAD}">🌐</tg-emoji> '
                            "Скачать HTML отчёт"
                        ),
                        "callback_data": "download_html",
                        "style": "primary",
                    },
                ],
                [
                    {
                        "text": (
                            f'<tg-emoji id="{EMOJI_MENU}">🚀</tg-emoji> '
                            "Перейти в бота"
                        ),
                        "url": f"https://t.me/{BOT_USERNAME}",
                        "style": "success",
                    }
                ],
            ]
        }

    def get_admin_keyboard(self) -> Dict:
        return {
            "inline_keyboard": [
                [
                    self._button(
                        f'<tg-emoji id="{EMOJI_ID}">📊</tg-emoji> Статистика',
                        "admin_stats",
                        "primary",
                    )
                ],
                [
                    self._button(
                        f'<tg-emoji id="{EMOJI_ID}">📣</tg-emoji> Рассылка',
                        "admin_broadcast",
                        "danger",
                    )
                ],
                [
                    self._button(
                        f'<tg-emoji id="{EMOJI_BACK}">◀️</tg-emoji> Назад',
                        "back",
                        "primary",
                    )
                ],
            ]
        }

    def get_back_keyboard(self) -> Dict:
        return {
            "inline_keyboard": [
                [
                    self._button(
                        f'<tg-emoji id="{EMOJI_BACK}">◀️</tg-emoji> Назад',
                        "back",
                        "primary",
                    )
                ]
            ]
        }

    # ────────────────────────────────────────────────────────────────────
    # MENU / PHOTO
    # ────────────────────────────────────────────────────────────────────

    def main_caption(self) -> str:
        return (
            f'<b><tg-emoji id="{EMOJI_BOT}">📍</tg-emoji> '
            "RegDateID Bot</b>\n\n"
            f'<tg-emoji id="{EMOJI_ID}">👾</tg-emoji> Выберите действие ниже.'
        )

    def _show_menu(
        self,
        chat_id: int,
        caption: str,
        keyboard: Dict,
        force_new: bool = False,
    ) -> Optional[Dict]:
        existing_id = self.menu_messages.get(chat_id)

        if not force_new and existing_id:
            edited = self.edit_message_caption(
                chat_id,
                existing_id,
                caption,
                keyboard,
            )
            if edited and edited.get("ok"):
                return edited

            edited_text = self.edit_message(
                chat_id,
                existing_id,
                caption,
                keyboard,
            )
            if edited_text and edited_text.get("ok"):
                return edited_text

        response = self.send_photo(chat_id, caption, keyboard)
        if response and response.get("result"):
            self.menu_messages[chat_id] = response["result"]["message_id"]
        return response

    # ────────────────────────────────────────────────────────────────────
    # STORAGE
    # ────────────────────────────────────────────────────────────────────

    def register_user(
        self,
        user_id: int,
        username: Optional[str] = None,
        reg_date: Optional[str] = None,
        timestamp: Optional[int] = None,
    ) -> None:
        try:
            storage.register_user(user_id, username, reg_date, timestamp)
        except Exception:
            logger.exception("database register error")

    def get_stats(self) -> int:
        try:
            return storage.get_stats()
        except Exception:
            logger.exception("stats error")
            return 0

    def _store_result(
        self,
        chat_id: int,
        target_id: int,
        reg_date: datetime,
        timestamp: int,
        username: Optional[str],
        result_text: str,
    ) -> None:
        data = {
            "result_text": result_text,
            "target_id": target_id,
            "reg_date": reg_date,
            "timestamp": timestamp,
            "username": username,
        }

        # В памяти хранится только последний результат для кнопок скачивания.
        # Полная история сохраняется в SQLite.
        self.user_data[chat_id] = data

        try:
            storage.save_analysis(
                chat_id=chat_id,
                target_id=target_id,
                username=username,
                registration_date=reg_date.strftime("%d.%m.%Y %H:%M:%S"),
                calculated_timestamp=timestamp,
                result_text=result_text,
            )
        except Exception:
            logger.exception("failed to save analysis history")

    # ────────────────────────────────────────────────────────────────────
    # ANALYSIS
    # ────────────────────────────────────────────────────────────────────

    def analyze_and_display(
        self,
        chat_id: int,
        target_id: int,
        username: Optional[str] = None,
    ) -> Tuple[str, datetime, int]:
        timestamp = self.analyzer.calculate_timestamp(target_id)
        reg_date = datetime.fromtimestamp(timestamp / 1000)
        years, months, days = self.analyzer.calculate_age(reg_date)
        precision = self.analyzer.get_precision(target_id)

        result_text = (
            f'<b><tg-emoji id="{EMOJI_ID}">👾</tg-emoji> ID:</b> '
            f'<code>{target_id}</code>\n'
        )

        if username:
            result_text += (
                f'<b><tg-emoji id="{EMOJI_BOT}">📍</tg-emoji> '
                f'Username:</b> <code>@{username}</code>\n'
            )

        result_text += (
            f'\n<b><tg-emoji id="{EMOJI_DATE}">👾</tg-emoji> '
            f'Регистрация:</b> <code>{reg_date:%d.%m.%Y %H:%M:%S}</code>\n'
            f'<b><tg-emoji id="{EMOJI_AGE}">👾</tg-emoji> '
            f'Возраст:</b> <code>{years} лет, {months} мес, {days} дн</code>\n'
            f'<b><tg-emoji id="{EMOJI_ACCURACY}">⚙️</tg-emoji> '
            f'Точность:</b> <i>{precision}</i>\n\n'
            f'<b><tg-emoji id="{EMOJI_BOT}">📍</tg-emoji> '
            f'Юзернейм бота:</b> <code>@{BOT_USERNAME}</code>'
        )
        return result_text, reg_date, timestamp

    def _deliver_result(
        self,
        chat_id: int,
        target_id: int,
        username: Optional[str] = None,
    ) -> None:
        try:
            result_text, reg_date, timestamp = self.analyze_and_display(
                chat_id,
                target_id,
                username,
            )
            self._store_result(
                chat_id,
                target_id,
                reg_date,
                timestamp,
                username,
                result_text,
            )

            # Результат ВСЕГДА отправляется новым сообщением.
            # Поэтому предыдущие ID/результаты остаются в чате.
            self.send_message(
                chat_id,
                result_text,
                self.get_result_keyboard(),
            )
        except Exception:
            logger.exception("result delivery error")
            self.send_message(
                chat_id,
                (
                    f'<tg-emoji id="{EMOJI_ACCURACY}">⚠️</tg-emoji> '
                    "<b>Не удалось выполнить анализ.</b>"
                ),
                self.get_back_keyboard(),
            )

    # ────────────────────────────────────────────────────────────────────
    # BROADCAST
    # ────────────────────────────────────────────────────────────────────

    def broadcast(self, message: str) -> int:
        try:
            users = storage.all_user_ids()
        except Exception:
            logger.exception("broadcast: cannot load users")
            return 0

        count = 0
        for user_id in users:
            try:
                response = self.send_message(user_id, message)
                if response and response.get("ok"):
                    count += 1
            except Exception:
                logger.exception("broadcast error for %s", user_id)

            # Не создаём резкий burst запросов.
            time.sleep(0.05)

        return count

    # ────────────────────────────────────────────────────────────────────
    # REPORTS
    # ────────────────────────────────────────────────────────────────────

    def _send_report_file(
        self,
        chat_id: int,
        callback_id: Optional[str],
        kind: str,
        target_id: Optional[int] = None,
    ) -> None:
        data = self.user_data.get(chat_id)

        def notify(text: str) -> None:
            if callback_id:
                self.answer_callback(callback_id, text)

        if target_id is None and data:
            target_id = data.get("target_id")

        if target_id is None:
            notify( "Нет сохранённого результата.")
            return

        try:
            target_id = int(target_id)
            if target_id < 1:
                raise ValueError
        except (TypeError, ValueError, OverflowError):
            notify( "Некорректный ID.")
            return

        try:
            if data and data.get("target_id") == target_id:
                reg_date = data["reg_date"]
                timestamp = data["timestamp"]
                username = data.get("username")
            else:
                timestamp = self.analyzer.calculate_timestamp(target_id)
                reg_date = datetime.fromtimestamp(timestamp / 1000)
                username = None

            years, months, days = self.analyzer.calculate_age(reg_date)
            precision = self.analyzer.get_precision(target_id)

            if kind == "txt":
                content = generate_txt_report(
                    target_id,
                    reg_date,
                    timestamp,
                    years,
                    months,
                    days,
                    precision,
                    len(MILESTONES),
                    username,
                )
                ext = "txt"
            elif kind == "html":
                content = generate_html_report(
                    target_id,
                    reg_date,
                    timestamp,
                    years,
                    months,
                    days,
                    precision,
                    len(MILESTONES),
                    username,
                )
                ext = "html"
            else:
                notify( "Неизвестный формат.")
                return

            filename = os.path.join(
                REPORTS_DIR,
                f"report_{target_id}_{int(time.time() * 1000)}.{ext}",
            )

            with open(filename, "w", encoding="utf-8") as report_file:
                report_file.write(content)

            try:
                with open(filename, "rb") as report_file:
                    response = self._make_request(
                        "sendDocument",
                        {"chat_id": str(chat_id)},
                        {
                            "document": (
                                f"report_{target_id}.{ext}",
                                report_file,
                                "text/plain"
                                if ext == "txt"
                                else "text/html",
                            )
                        },
                        retry_count=2,
                        timeout=(2.0, 20.0),
                    )

                if response and response.get("ok"):
                    notify(f"{ext.upper()} отчёт отправлен.")
                else:
                    notify( "Не удалось отправить отчёт.")
            finally:
                try:
                    os.remove(filename)
                except OSError:
                    pass

        except Exception:
            logger.exception("report generation/send error")
            notify( "Ошибка формирования отчёта.")

    # ────────────────────────────────────────────────────────────────────
    # UPDATE LOOP
    # ────────────────────────────────────────────────────────────────────

    def process_updates(self) -> None:
        while True:
            try:
                response = self._make_request(
                    "getUpdates",
                    {
                        "offset": self.offset,
                        "timeout": 25,
                        "allowed_updates": [
                            "message",
                            "callback_query",
                            "inline_query",
                        ],
                    },
                    retry_count=3,
                    timeout=(3.0, 35.0),
                )

                if response and response.get("ok"):
                    for update in response.get("result", []):
                        self.offset = update["update_id"] + 1

                        try:
                            if "inline_query" in update:
                                # Inline никогда не должен блокировать polling:
                                # обрабатываем его отдельно.
                                threading.Thread(
                                    target=self._safe_inline_thread,
                                    args=(update["inline_query"],),
                                    daemon=True,
                                ).start()
                            elif "message" in update:
                                self.handle_message(update["message"])
                            elif "callback_query" in update:
                                self.handle_callback(update["callback_query"])
                        except Exception:
                            logger.exception("update handling error")

                time.sleep(0.05)

            except Exception:
                logger.exception("updates loop error")
                time.sleep(1.0)

    def _safe_inline_thread(self, inline_query: Dict) -> None:
        try:
            self.handle_inline_query(inline_query)
        except Exception:
            logger.exception("inline thread crashed")

    # ────────────────────────────────────────────────────────────────────
    # MESSAGES
    # ────────────────────────────────────────────────────────────────────

    @staticmethod
    def _parse_positive_id(value: str) -> Optional[int]:
        if not isinstance(value, str):
            return None

        value = value.strip()
        if not value or not value.isdigit() or len(value) > 20:
            return None

        try:
            number = int(value)
        except (TypeError, ValueError, OverflowError):
            return None

        if number < 1 or number > 20_000_000_000:
            return None

        return number

    def handle_message(self, message: Dict) -> None:
        chat = message.get("chat") or {}
        sender = message.get("from") or {}
        chat_id = chat.get("id")
        user_id = sender.get("id")
        username = sender.get("username")

        if chat_id is None:
            return

        text = message.get("text")
        text = text.strip() if isinstance(text, str) else None

        if text and text.startswith("/start"):
            # ВАЖНО: /start НЕ удаляем.
            payload = text[6:].strip()

            if payload.startswith("download_txt_"):
                target = self._parse_positive_id(
                    payload[len("download_txt_"):]
                )
                if target is not None:
                    self._send_report_file(
                        chat_id,
                        f"deep_start_{chat_id}",
                        "txt",
                        target,
                    )
                self._show_menu(
                    chat_id,
                    self.main_caption(),
                    self.get_main_keyboard(user_id == self.admin_id),
                    force_new=False,
                )
                self.user_states[chat_id] = None
                return

            if payload.startswith("download_html_"):
                target = self._parse_positive_id(
                    payload[len("download_html_"):]
                )
                if target is not None:
                    self._send_report_file(
                        chat_id,
                        f"deep_start_{chat_id}",
                        "html",
                        target,
                    )
                self._show_menu(
                    chat_id,
                    self.main_caption(),
                    self.get_main_keyboard(user_id == self.admin_id),
                    force_new=False,
                )
                self.user_states[chat_id] = None
                return

            self._show_menu(
                chat_id,
                self.main_caption(),
                self.get_main_keyboard(user_id == self.admin_id),
            )
            self.user_states[chat_id] = None
            return

        if text and text.lower().startswith("/reg1"):
            parts = text.split(maxsplit=1)
            target_id = (
                self._parse_positive_id(parts[1])
                if len(parts) >= 2
                else None
            )

            if target_id is None:
                self.send_message(
                    chat_id,
                    (
                        f'<tg-emoji id="{EMOJI_ACCURACY}">⚠️</tg-emoji> '
                        "<b>Использование:</b> "
                        "<code>/reg1 123456789</code>"
                    ),
                )
                return

            self._deliver_result(chat_id, target_id)
            return

        state = self.user_states.get(chat_id)

        if text and state == "waiting_id":
            target_id = self._parse_positive_id(text)

            if target_id is None:
                self.send_message(
                    chat_id,
                    (
                        f'<tg-emoji id="{EMOJI_ACCURACY}">⚠️</tg-emoji> '
                        "<b>Введите корректный числовой ID.</b>"
                    ),
                    self.get_back_keyboard(),
                )
            else:
                self._deliver_result(chat_id, target_id)
                self.user_states[chat_id] = None
            return

        if text and state == "waiting_broadcast":
            sent_count = self.broadcast(text)
            self.send_message(
                chat_id,
                (
                    f'<tg-emoji id="{EMOJI_ID}">📣</tg-emoji> '
                    f"<b>Рассылка завершена.</b>\n\n"
                    f"Отправлено: <code>{sent_count}</code>"
                ),
                self.get_back_keyboard(),
            )
            self.user_states[chat_id] = None
            return

        if (
            message.get("forward_from")
            and state == "waiting_forward"
        ):
            forward_from = message["forward_from"]
            target_id = self._parse_positive_id(
                str(forward_from.get("id", ""))
            )
            username_forward = forward_from.get("username")

            if target_id is not None:
                self._deliver_result(
                    chat_id,
                    target_id,
                    username_forward,
                )
                self.user_states[chat_id] = None
            return

        # Числовой ID без меню.
        if text:
            target_id = self._parse_positive_id(text)
            if target_id is not None:
                self._deliver_result(chat_id, target_id)
                return

    # ────────────────────────────────────────────────────────────────────
    # CALLBACKS
    # ────────────────────────────────────────────────────────────────────

    def handle_callback(self, callback: Dict) -> None:
        callback_id = callback.get("id")
        callback_message = callback.get("message") or {}
        chat = callback_message.get("chat") or {}

        chat_id = chat.get("id")
        message_id = callback_message.get("message_id")
        data = callback.get("data", "")
        user = callback.get("from") or {}

        if callback_id is None or chat_id is None or message_id is None:
            return

        user_id = user.get("id", 0)
        username = user.get("username")

        try:
            if data == "my_id_reg":
                result_text, reg_date, timestamp = self.analyze_and_display(
                    chat_id,
                    chat_id,
                    username,
                )
                self.register_user(
                    chat_id,
                    username,
                    reg_date.strftime("%d.%m.%Y %H:%M:%S"),
                    timestamp,
                )
                self._store_result(
                    chat_id,
                    chat_id,
                    reg_date,
                    timestamp,
                    username,
                    result_text,
                )

                # Не редактируем старое меню в результат.
                # Результат остаётся отдельным сообщением.
                self.send_message(
                    chat_id,
                    result_text,
                    self.get_result_keyboard(),
                )
                self.answer_callback(callback_id, "Готово.")
                return

            if data == "method_id":
                self._edit_menu_or_text(
                    chat_id,
                    message_id,
                    (
                        f'<b><tg-emoji id="{EMOJI_ID}">🔎</tg-emoji> '
                        "Введите ID аккаунта:</b>"
                    ),
                    self.get_back_keyboard(),
                )
                self.user_states[chat_id] = "waiting_id"
                self.answer_callback(callback_id)
                return

            if data == "method_forward":
                self._edit_menu_or_text(
                    chat_id,
                    message_id,
                    (
                        f'<b><tg-emoji id="{EMOJI_ID}">📨</tg-emoji> '
                        "Перешлите сообщение от пользователя.</b>"
                    ),
                    self.get_back_keyboard(),
                )
                self.user_states[chat_id] = "waiting_forward"
                self.answer_callback(callback_id)
                return

            if data == "instructions":
                self._edit_menu_or_text(
                    chat_id,
                    message_id,
                    INSTRUCTIONS_TEXT,
                    self.get_back_keyboard(),
                )
                self.user_states[chat_id] = None
                self.answer_callback(callback_id)
                return

            if data == "download_txt":
                self._send_report_file(chat_id, callback_id, "txt")
                return

            if data == "download_html":
                self._send_report_file(chat_id, callback_id, "html")
                return

            if data == "admin_panel":
                if user_id != self.admin_id:
                    self.answer_callback(callback_id, "Доступ запрещён.")
                    return

                self._edit_menu_or_text(
                    chat_id,
                    message_id,
                    (
                        f'<b><tg-emoji id="{EMOJI_MENU}">⚙️</tg-emoji> '
                        "Админ-панель</b>"
                    ),
                    self.get_admin_keyboard(),
                )
                self.answer_callback(callback_id)
                return

            if data == "admin_stats":
                if user_id != self.admin_id:
                    self.answer_callback(callback_id, "Доступ запрещён.")
                    return

                stats = self.get_stats()
                self._edit_menu_or_text(
                    chat_id,
                    message_id,
                    (
                        f'<b><tg-emoji id="{EMOJI_ID}">📊</tg-emoji> '
                        "Статистика</b>\n\n"
                        f"Пользователей: <code>{stats}</code>"
                    ),
                    self.get_back_keyboard(),
                )
                self.answer_callback(callback_id)
                return

            if data == "admin_broadcast":
                if user_id != self.admin_id:
                    self.answer_callback(callback_id, "Доступ запрещён.")
                    return

                self._edit_menu_or_text(
                    chat_id,
                    message_id,
                    (
                        f'<b><tg-emoji id="{EMOJI_ID}">📣</tg-emoji> '
                        "Введите текст рассылки:</b>"
                    ),
                    self.get_back_keyboard(),
                )
                self.user_states[chat_id] = "waiting_broadcast"
                self.answer_callback(callback_id)
                return

            if data == "back":
                self._show_menu(
                    chat_id,
                    self.main_caption(),
                    self.get_main_keyboard(user_id == self.admin_id),
                )
                self.user_states[chat_id] = None
                self.answer_callback(callback_id)
                return

            self.answer_callback(callback_id)

        except Exception:
            logger.exception("callback error")
            self.answer_callback(callback_id, "Произошла ошибка.")

    def _edit_menu_or_text(
        self,
        chat_id: int,
        message_id: int,
        text: str,
        keyboard: Dict,
    ) -> None:
        response = self.edit_message_caption(
            chat_id,
            message_id,
            text,
            keyboard,
        )
        if response and response.get("ok"):
            return

        self.edit_message(
            chat_id,
            message_id,
            text,
            keyboard,
        )

    # ────────────────────────────────────────────────────────────────────
    # INLINE
    # ────────────────────────────────────────────────────────────────────

    def handle_inline_query(self, inline_query: Dict) -> None:
        query_id = inline_query.get("id")
        query_text = inline_query.get("query", "")

        if not query_id:
            return

        try:
            results, error = build_inline_results(
                self.inline_handler,
                query_text,
            )

            if not results and error:
                results = [
                    {
                        "type": "article",
                        "id": "inline_error",
                        "title": "Ошибка",
                        "description": "Проверьте Telegram ID.",
                        "input_message_content": {
                            "message_text": error,
                            "parse_mode": "HTML",
                        },
                    }
                ]

            elif not results:
                results = [
                    {
                        "type": "article",
                        "id": "inline_help",
                        "title": "Введите Telegram ID",
                        "description": "Например: 123456789",
                        "input_message_content": {
                            "message_text": (
                                f'<b><tg-emoji id="{EMOJI_ID}">👾</tg-emoji> '
                                "Inline-режим</b>\n\n"
                                "Введите числовой Telegram ID."
                            ),
                            "parse_mode": "HTML",
                        },
                    }
                ]

            self.answer_inline_query(
                query_id,
                results,
                cache_time=5,
            )

        except Exception:
            logger.exception("inline handler error")
            # Даже при непредвиденной ошибке Telegram получает ответ.
            self.answer_inline_query(
                query_id,
                [
                    {
                        "type": "article",
                        "id": "inline_fallback",
                        "title": "Попробуйте ещё раз",
                        "description": "Не удалось обработать запрос.",
                        "input_message_content": {
                            "message_text": (
                                f'<tg-emoji id="{EMOJI_ACCURACY}">⚠️</tg-emoji> '
                                "<b>Не удалось обработать запрос.</b>"
                            ),
                            "parse_mode": "HTML",
                        },
                    }
                ],
                cache_time=1,
            )


def run() -> None:
    if not TOKEN:
        print("[regbot] BOT_TOKEN_DATA не задан — бот не запущен")
        return

    if not os.path.isfile(MENU_IMAGE):
        logger.warning(
            "foto.png не найден. Положите его рядом с bot.py "
            "или задайте REGBOT_MENU_IMAGE."
        )

    print("[regbot] бот запущен, начинаю polling")
    bot = TelegramBot(TOKEN, ADMIN_ID)
    bot.process_updates()
