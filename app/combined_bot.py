"""
combined bot — один Telegram-бот (один токен), в котором уживаются
два независимых набора функций:

  • "сингулярное ядро" — idle-игра (.core/.status/.cluster/.help)
  • экономика GRAM — виртуальная валюта и мини-игры (.бб, .куш, .мины, .джокер, .рул, ...)

Обработчики зарегистрированы на ОДНОМ экземпляре telebot.TeleBot, чтобы не было
конфликта getUpdates (два polling-цикла с одним токеном работать не могут).
Наборы команд и callback_data у модулей не пересекаются, поэтому они не мешают
друг другу.
"""

import os
import telebot

from .singularity import db as sdb
from .singularity import handlers as shandlers
from .economy import db as edb
from .economy import handlers as ehandlers

TOKEN = os.environ.get("BOT_TOKEN", "")

bot = telebot.TeleBot(TOKEN, parse_mode=None)

# порядок регистрации не важен: у каждого модуля свой непересекающийся
# набор текстовых команд и callback_data
shandlers.register(bot)
ehandlers.register(bot)


def run():
    if not TOKEN:
        print("[combined] BOT_TOKEN не задан — бот не запущен")
        return
    sdb.init()
    edb.init()
    print("[combined] бот запущен, начинаю polling")
    bot.infinity_polling(timeout=30, long_polling_timeout=20)
