import re
from . import db, core, ui

# state manager: {user_id: "window_name"}
user_states = {}

# the only prefix-commands this module owns — kept narrow on purpose so it
# never swallows commands that belong to the economy module sharing the bot.
_KNOWN_CMDS = {"core", "status", "cluster", "help"}
_PREFIX_RE = re.compile(r"^[.!](core|status|cluster|help)\b", re.IGNORECASE)

_CALLBACKS = {
    "collect", "upgrade", "cooldown",
    "nav_status", "nav_cluster", "nav_help", "nav_main",
    "cluster_join", "cluster_leave", "cluster_collect",
}


def get_state(uid):
    return user_states.get(uid, "main")


def set_state(uid, state):
    user_states[uid] = state


def register(bot):

    @bot.message_handler(commands=["start"])
    def cmd_start(msg):
        uid = msg.from_user.id
        username = msg.from_user.username or f"user_{uid}"
        db.ensure_user(uid, username)
        set_state(uid, "main")
        sent = bot.send_message(
            msg.chat.id,
            ui.render_main(uid),
            reply_markup=ui.kb_main(),
        )
        db.set_msg_id(uid, sent.message_id, msg.chat.id)

    @bot.message_handler(regexp=_PREFIX_RE.pattern)
    def cmd_prefix(msg):
        uid = msg.from_user.id
        username = msg.from_user.username or f"user_{uid}"
        db.ensure_user(uid, username)
        match = _PREFIX_RE.match(msg.text.strip())
        if not match:
            return
        cmd = match.group(1).lower()
        _route_prefix(cmd, msg)

    def _route_prefix(cmd, msg):
        handlers = {
            "core": lambda: _open_window(msg, "main"),
            "status": lambda: _open_window(msg, "status"),
            "cluster": lambda: _open_window(msg, "cluster"),
            "help": lambda: _open_window(msg, "help"),
        }
        fn = handlers.get(cmd)
        if fn:
            fn()

    def _open_window(msg, window):
        uid = msg.from_user.id
        chat_id = msg.chat.id
        set_state(uid, window)
        text, kb = _render_window(uid, window, chat_id)
        row = db.get_msg(uid)
        if row and row["chat_id"] == chat_id:
            try:
                bot.edit_message_text(text, chat_id, row["msg_id"], reply_markup=kb)
                return
            except Exception:
                pass
        sent = bot.send_message(chat_id, text, reply_markup=kb)
        db.set_msg_id(uid, sent.message_id, chat_id)

    @bot.callback_query_handler(func=lambda c: c.data in _CALLBACKS)
    def on_callback(call):
        uid = call.from_user.id
        chat_id = call.message.chat.id
        msg_id = call.message.message_id
        db.ensure_user(uid, call.from_user.username or f"user_{uid}")

        data = call.data

        if data == "collect":
            core.collect(uid)
            set_state(uid, "main")
        elif data == "upgrade":
            core.upgrade(uid)
            set_state(uid, "main")
        elif data == "cooldown":
            core.cooldown(uid)
            set_state(uid, "main")
        elif data == "nav_status":
            set_state(uid, "status")
        elif data == "nav_cluster":
            set_state(uid, "cluster")
        elif data == "nav_help":
            set_state(uid, "help")
        elif data == "nav_main":
            set_state(uid, "main")
        elif data == "cluster_join":
            core.cluster_join(uid, chat_id)
            set_state(uid, "cluster")
        elif data == "cluster_leave":
            core.cluster_leave(uid)
            set_state(uid, "cluster")
        elif data == "cluster_collect":
            core.cluster_collect(uid, chat_id)
            set_state(uid, "cluster")

        window = get_state(uid)
        text, kb = _render_window(uid, window, chat_id)

        try:
            bot.edit_message_text(text, chat_id, msg_id, reply_markup=kb)
        except Exception:
            pass

        bot.answer_callback_query(call.id)

    def _render_window(uid, window, chat_id):
        if window == "main":
            return ui.render_main(uid), ui.kb_main()
        elif window == "status":
            return ui.render_status(uid), ui.kb_status()
        elif window == "cluster":
            return ui.render_cluster(uid, chat_id), ui.kb_cluster(uid, chat_id)
        elif window == "help":
            return ui.render_help(), ui.kb_back()
        return ui.render_main(uid), ui.kb_main()
