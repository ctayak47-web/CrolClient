# Unified Telegram Bots

Объединённый Python-проект для запуска **4 Telegram-ботов** на **Render.com** с поддержкой **UptimeRobot**.

## 📋 Структура проекта

Проект содержит 4 независимых Telegram-бота, работающих параллельно:

### 1. **Combined Bot** (основной двойной бот)
- **Токен:** `BOT_TOKEN`
- **Функционал:**
  - Сингулярное ядро (idle-игра)
  - Экономика GRAM (виртуальная валюта, мини-игры)

### 2. **RegBot** (анализ даты регистрации)
- **Токен:** `BOT_TOKEN_DATA`
- **Функционал:**
  - Анализ даты регистрации Telegram аккаунта по ID
  - Загрузка отчётов (.txt, .html)

### 3. **Profile Bot** (генератор профиля)
- **Токен:** `BOT_TOKEN_PROFILE`
- **Функционал:**
  - Генерация мокапа профиля Telegram в стиле iOS
  - Ввод имени, юзернейма, статуса, "о себе", фото
  - Выбор часового пояса

### 4. **CumBot** (новый КончаБот)
- **Токен:** `BOT_TOKEN_CUM`
- **Функционал:**
  - Команда/текст "выстрел" — стрелять
  - Выстрел с reply — стрелять по пользователю
  - Анимация процесса (edit_message_text)
  - Топ-10 лидеров по кончи
  - Админ-команды: `/дать` и `/забрать`
  - БД SQLite для хранения баланса кончи

## 🚀 Быстрый старт локально

### 1. Установка зависимостей

```bash
pip install -r requirements.txt --break-system-packages
```

### 2. Подготовка переменных окружения

Скопируй `.env.example` в `.env` и заполни все 4 токена:

```bash
cp .env.example .env
```

Отредактируй `.env`:

```env
BOT_TOKEN=your_combined_bot_token
BOT_TOKEN_DATA=your_regbot_token
BOT_TOKEN_PROFILE=your_profile_bot_token
BOT_TOKEN_CUM=your_cumbot_token
ADMIN_IDS=your_admin_id_1,your_admin_id_2
PORT=8080
```

### 3. Запуск проекта

```bash
python main.py
```

Flask сервер запустится на `http://localhost:8080`, а все 4 бота начнут polling в отдельных потоках.

## 🌐 Развёртывание на Render.com

### 1. Создай репозиторий на GitHub

```bash
git init
git add .
git commit -m "initial commit"
git remote add origin https://github.com/your-username/unified-telegram-bots.git
git push -u origin main
```

### 2. Создай веб-сервис на Render.com

1. Перейди на [render.com](https://render.com)
2. Нажми "New +" → "Web Service"
3. Выбери свой GitHub репозиторий
4. Заполни настройки:
   - **Name:** `unified-telegram-bots`
   - **Environment:** `Python 3`
   - **Build Command:** `pip install -r requirements.txt`
   - **Start Command:** `python main.py`
   - **Plan:** Free (или выше)

### 3. Добавь переменные окружения

В настройках сервиса добавь Environment Variables:

```
BOT_TOKEN = your_combined_bot_token
BOT_TOKEN_DATA = your_regbot_token
BOT_TOKEN_PROFILE = your_profile_bot_token
BOT_TOKEN_CUM = your_cumbot_token
ADMIN_IDS = your_admin_id_1,your_admin_id_2
PORT = 8080
CUMBOT_DB_PATH = /var/data/cumbot.db
REGBOT_REPORTS_DIR = /var/data/reports
```

### 4. UptimeRobot интеграция

Используй URL сервиса на Render для пинга:

- **Monitoring URL:** `https://your-service.onrender.com/ping`
- **Интервал:** 5 минут

Это предотвратит засыпание бесплатного сервиса.

## 📚 Структура файлов

```
unified_telegram_bots/
├── main.py                          # Главный файл запуска
├── requirements.txt                 # Зависимости Python
├── .env.example                    # Пример переменных окружения
├── render.yaml                     # Конфигурация для Render.com
├── README.md                       # Этот файл
├── render.py                       # Рендеринг профиля iOS
├── sjad.py                         # (устарелый, для совместимости)
├── fonts/                          # Шрифты для профиля
│   ├── DejaVuSans.ttf
│   └── DejaVuSans-Bold.ttf
├── static/                         # Статические файлы
│   └── index.html
├── data/                           # Данные (создаётся автоматически)
│   ├── cumbot.db                   # БД КончаБота
│   └── reports/                    # Отчёты RegBot
└── app/                            # Python модули
    ├── __init__.py
    ├── combined_bot.py             # Combined Bot (основной)
    ├── profile_bot.py              # Profile Bot (адаптированный)
    ├── cumbot_runner.py            # CumBot runner
    ├── singularity/                # Сингулярное ядро (combined)
    ├── economy/                    # Экономика GRAM (combined)
    ├── regbot/                     # RegBot модули
    └── cumbot/                     # CumBot модули
        ├── __init__.py
        ├── db.py                   # БД кончи
        └── handlers.py             # Обработчики команд
```

## 🎮 Команды КончаБота

### Основные команды

- **"выстрел"** (текст или команда `/выстрел`)
  - Обычный выстрел: добавляет 1-3 кончи
  - Выстрел с reply: добавляет 3-5 кончи, целится в пользователя
  - Анимация: 3-4 кадра с действиями

- **"/лидеры"** или **"/leaders"**
  - Выводит топ-10 пользователей по кончи

### Админ-команды (требуют прав)

- **"/дать <количество>"** (ответом на сообщение пользователя)
  - Дать кончу пользователю

- **"/забрать <количество>"** (ответом на сообщение пользователя)
  - Забрать кончу у пользователя

## 🔧 Конфигурация

### Переменные окружения

| Переменная | Тип | Обязательная | Описание |
|---|---|---|---|
| `BOT_TOKEN` | str | ✓ | Токен Combined Bot |
| `BOT_TOKEN_DATA` | str | ✓ | Токен RegBot |
| `BOT_TOKEN_PROFILE` | str | ✓ | Токен Profile Bot |
| `BOT_TOKEN_CUM` | str | ✓ | Токен CumBot |
| `ADMIN_IDS` | str | ✓ | ID админов (через запятую) |
| `PORT` | int | ✗ | Порт Flask (по умолчанию 8080) |
| `CUMBOT_DB_PATH` | str | ✗ | Путь к БД КончаБота |
| `REGBOT_REPORTS_DIR` | str | ✗ | Путь к отчётам RegBot |

## 📝 Получение токенов

1. Напиши [@BotFather](https://t.me/botfather) в Telegram
2. Команда `/newbot`
3. Введи имя и юзернейм бота
4. Скопируй полученный токен в `.env`

## 🐛 Отладка

### Логи на Render.com

```bash
# В консоли Render смотри вывод:
[combined_bot] бот запущен, начинаю polling
[regbot] бот запущен, начинаю polling
[profile_bot] бот запущен, начинаю polling
[cumbot] бот запущен, начинаю polling
```

### Проверка здоровья

```bash
curl https://your-service.onrender.com/health
```

Ответ:

```json
{
  "status": "ok",
  "combined_bot": true,
  "regbot": true,
  "profile_bot": true,
  "cumbot": true
}
```

## 📞 Поддержка

Если возникают ошибки, проверь:

1. ✅ Все 4 токена корректны и активны
2. ✅ ADMIN_IDS содержит твой Telegram ID
3. ✅ PORT = 8080 (для Render)
4. ✅ Интернет-соединение стабильно
5. ✅ UptimeRobot пингует `/ping` каждые 5 минут

## 📄 Лицензия

MIT
