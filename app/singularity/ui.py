from telebot import types
from . import core, db
from datetime import datetime, timezone

# ─── progress bar renderer ────────────────────────────────────────────────────

def _bar(value, maximum, width=20, fill="█", empty="░"):
    filled = int(round(width * min(value, maximum) / maximum)) if maximum else 0
    return fill * filled + empty * (width - filled)


def _heat_bar(heat):
    pct = heat / 100.0
    bar = _bar(heat, 100, width=16)
    if pct < 0.4:
        tag = "норм"
    elif pct < 0.7:
        tag = "тепл"
    elif pct < 0.9:
        tag = "жар "
    else:
        tag = "КРИТ"
    return f"[{tag}] {bar} {heat:4.0f}%"


def _fmt_cycles(n):
    if n >= 1_000_000:
        return f"{n/1_000_000:.2f}м"
    if n >= 1_000:
        return f"{n/1_000:.2f}к"
    return f"{n:.1f}"


def _elapsed_str(ts_str):
    if ts_str is None:
        return "никогда"
    try:
        dt = datetime.fromisoformat(str(ts_str))
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)
        secs = int((datetime.now(timezone.utc) - dt).total_seconds())
        if secs < 60:
            return f"{secs}с назад"
        if secs < 3600:
            return f"{secs//60}м назад"
        if secs < 86400:
            return f"{secs//3600}ч {(secs%3600)//60}м назад"
        return f"{secs//86400}д назад"
    except Exception:
        return "?"


# ─── windows ─────────────────────────────────────────────────────────────────

def render_main(uid):
    s = core.user_stats(uid)
    if not s:
        return "инициализация ядра..."

    lines = [
        "┌─ сингулярное ядро ──────────────────┐",
        f"│  узел #{uid}",
        f"│  уровень : {s['power_level']}",
        f"│  баланс  : {_fmt_cycles(s['balance'])} циклов",
        "│",
        f"│  доступно: +{_fmt_cycles(s['pending'])} циклов",
        f"│  скорость: {s['effective_rate']:.2f} ц/с  (кпд {s['efficiency']*100:.0f}%)",
        "│",
        f"│  нагрев  : {_heat_bar(s['heat'])}",
        "│",
        f"│  последний сбор: {_elapsed_str(s['last_collect'])}",
        "└─────────────────────────────────────┘",
    ]
    return "\n".join(lines)


def render_status(uid):
    s = core.user_stats(uid)
    if not s:
        return "нет данных"

    upgrade_affordable = "✓" if s["balance"] >= s["upgrade_cost"] else "✗"
    cool_affordable = "✓" if s["balance"] >= 50 else "✗"

    lines = [
        "┌─ статус узла ───────────────────────┐",
        f"│  уровень мощности : {s['power_level']}",
        f"│  базовая скорость : {s['rate']:.2f} ц/с",
        f"│  кпд              : {s['efficiency']*100:.0f}%",
        f"│  эффект. скорость : {s['effective_rate']:.2f} ц/с",
        "│",
        f"│  нагрев  : {s['heat']:.1f} / 100",
        f"│  {_bar(s['heat'], 100, 30)}",
        "│",
        f"│  баланс  : {_fmt_cycles(s['balance'])} циклов",
        f"│  апгрейд до ур.{s['power_level']+1}: {_fmt_cycles(s['upgrade_cost'])} [{upgrade_affordable}]",
        f"│  аварийное охлаждение: 50 ц [{cool_affordable}]",
        "└─────────────────────────────────────┘",
    ]
    return "\n".join(lines)


def render_cluster(uid, chat_id):
    cs = core.cluster_stats(uid, chat_id)
    in_c = cs["in_cluster"]

    if chat_id and chat_id < 0:
        lines = [
            "┌─ кластерный интерфейс ──────────────┐",
            f"│  чат     : {chat_id}",
            f"│  участников : {cs['member_count']}",
            f"│  бонус   : x{cs['bonus_mult']:.2f}",
            "│",
        ]
        if cs["members"]:
            lines.append("│  активные узлы:")
            for m in cs["members"][:8]:
                name = m["username"] or f"#{m['user_id']}"
                lines.append(f"│    [{m['power_level']:2d}] {name[:20]}")
        else:
            lines.append("│  в кластере нет узлов")
        status = "подключён" if in_c else "не подключён"
        lines += [
            "│",
            f"│  ваш статус: {status}",
            "└─────────────────────────────────────┘",
        ]
    else:
        lines = [
            "┌─ кластерный интерфейс ──────────────┐",
            "│  кластеры доступны только в         │",
            "│  групповых чатах. добавьте бота     │",
            "│  в группу для использования.        │",
            "└─────────────────────────────────────┘",
        ]
    return "\n".join(lines)


def render_help():
    lines = [
        "┌─ справка ───────────────────────────┐",
        "│",
        "│  команды",
        "│  .core    / !core    – главная панель",
        "│  .status  / !status  – статус узла",
        "│  .cluster / !cluster – кластер",
        "│  .help    / !help    – эта справка",
        "│",
        "│  механики",
        "│  – циклы накапливаются со временем",
        "│  – каждый сбор повышает нагрев",
        "│  – нагрев остывает пассивно",
        "│  – высокий нагрев снижает кпд",
        "│  – стоимость апгрейда растёт",
        "│  – кластер даёт общий бонус",
        "│    (+15% за каждого участника)",
        "│",
        "│  совет: собирайте регулярно, но",
        "│  не слишком часто — управление",
        "│  нагревом это главный вызов.",
        "└─────────────────────────────────────┘",
    ]
    return "\n".join(lines)


# ─── keyboards ────────────────────────────────────────────────────────────────

def kb_main():
    kb = types.InlineKeyboardMarkup(row_width=2)
    kb.add(
        types.InlineKeyboardButton("[ собрать ]",   callback_data="collect"),
        types.InlineKeyboardButton("[ апгрейд ]",   callback_data="upgrade"),
    )
    kb.add(
        types.InlineKeyboardButton("[ охладить ]",  callback_data="cooldown"),
        types.InlineKeyboardButton("[ статус ]",    callback_data="nav_status"),
    )
    kb.add(
        types.InlineKeyboardButton("[ кластер ]",   callback_data="nav_cluster"),
        types.InlineKeyboardButton("[ справка ]",   callback_data="nav_help"),
    )
    return kb


def kb_status():
    kb = types.InlineKeyboardMarkup(row_width=2)
    kb.add(
        types.InlineKeyboardButton("[ собрать ]",   callback_data="collect"),
        types.InlineKeyboardButton("[ апгрейд ]",   callback_data="upgrade"),
    )
    kb.add(
        types.InlineKeyboardButton("[ охладить ]",  callback_data="cooldown"),
        types.InlineKeyboardButton("[ < назад ]",   callback_data="nav_main"),
    )
    return kb


def kb_cluster(uid, chat_id):
    kb = types.InlineKeyboardMarkup(row_width=2)
    cs = core.cluster_stats(uid, chat_id)

    if chat_id and chat_id < 0:
        if cs["in_cluster"]:
            kb.add(
                types.InlineKeyboardButton("[ собрать кластер ]", callback_data="cluster_collect"),
            )
            kb.add(
                types.InlineKeyboardButton("[ выйти ]",    callback_data="cluster_leave"),
                types.InlineKeyboardButton("[ < назад ]",  callback_data="nav_main"),
            )
        else:
            kb.add(
                types.InlineKeyboardButton("[ вступить ]", callback_data="cluster_join"),
                types.InlineKeyboardButton("[ < назад ]",  callback_data="nav_main"),
            )
    else:
        kb.add(types.InlineKeyboardButton("[ < назад ]", callback_data="nav_main"))
    return kb


def kb_back():
    kb = types.InlineKeyboardMarkup()
    kb.add(types.InlineKeyboardButton("[ < назад ]", callback_data="nav_main"))
    return kb
