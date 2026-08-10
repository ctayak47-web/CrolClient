"""
Точка входа для Render.com (Web Service).

Поднимает:
  1. Flask-сервер — нужен только для того, чтобы Render видел открытый порт
     и чтобы UptimeRobot мог пинговать "/" и не давать сервису засыпать.
  2. combined-бот (BOT_TOKEN) — сингулярное ядро + экономика GRAM.
  3. regbot (BOT_TOKEN_DATA) — анализ даты регистрации по id.

Нужно ровно 2 токена в ENV: BOT_TOKEN и BOT_TOKEN_DATA.
"""

import os
import threading

from flask import Flask, jsonify

from app import combined_bot
from app.regbot import bot as regbot

app = Flask(__name__, static_folder="static", static_url_path="")


@app.route("/")
def index():
    return app.send_static_file("index.html")


@app.route("/ping")
def ping():
    return jsonify({"status": "online"})


@app.route("/health")
def health():
    return jsonify({
        "status": "ok",
        "combined_bot": bool(os.environ.get("BOT_TOKEN")),
        "regbot": bool(os.environ.get("BOT_TOKEN_DATA")),
    })


def _run_safely(fn, name):
    try:
        fn()
    except Exception as e:
        print(f"[{name}] fatal error: {e}")


def main():
    threading.Thread(target=_run_safely, args=(combined_bot.run, "combined"), daemon=True).start()
    threading.Thread(target=_run_safely, args=(regbot.run, "regbot"), daemon=True).start()

    port = int(os.environ.get("PORT", 8080))
    app.run(host="0.0.0.0", port=port)


if __name__ == "__main__":
    main()
