import sqlite3
import os

DB_PATH = os.environ.get("SINGULARITY_DB_PATH", "data/singularity.db")


def _conn():
    os.makedirs(os.path.dirname(DB_PATH) or ".", exist_ok=True)
    con = sqlite3.connect(DB_PATH)
    con.row_factory = sqlite3.Row
    return con


def init():
    with _conn() as con:
        con.executescript("""
            create table if not exists users (
                user_id     integer primary key,
                username    text    default '',
                power_level integer default 1,
                last_collect text   default (datetime('now')),
                heat_level  real    default 0.0,
                balance     real    default 0.0,
                last_active text    default (datetime('now'))
            );

            create table if not exists messages (
                user_id  integer primary key,
                msg_id   integer,
                chat_id  integer
            );

            create table if not exists clusters (
                chat_id     integer primary key,
                last_collect text default (datetime('now')),
                total_power  integer default 0
            );

            create table if not exists cluster_members (
                user_id integer,
                chat_id integer,
                primary key (user_id, chat_id)
            );
        """)


def ensure_user(uid, username=""):
    with _conn() as con:
        con.execute("""
            insert or ignore into users (user_id, username)
            values (?, ?)
        """, (uid, username))
        con.execute("""
            update users set username=?, last_active=datetime('now')
            where user_id=?
        """, (username, uid))


def get_user(uid):
    with _conn() as con:
        return con.execute(
            "select * from users where user_id=?", (uid,)
        ).fetchone()


def update_user(uid, **kwargs):
    if not kwargs:
        return
    sets = ", ".join(f"{k}=?" for k in kwargs)
    vals = list(kwargs.values()) + [uid]
    with _conn() as con:
        con.execute(f"update users set {sets} where user_id=?", vals)


def set_msg_id(uid, msg_id, chat_id):
    with _conn() as con:
        con.execute("""
            insert or replace into messages (user_id, msg_id, chat_id)
            values (?, ?, ?)
        """, (uid, msg_id, chat_id))


def get_msg(uid):
    with _conn() as con:
        return con.execute(
            "select * from messages where user_id=?", (uid,)
        ).fetchone()


# ─── cluster ─────────────────────────────────────────────────────────────────

def cluster_ensure(chat_id):
    with _conn() as con:
        con.execute("""
            insert or ignore into clusters (chat_id)
            values (?)
        """, (chat_id,))


def cluster_join(uid, chat_id):
    cluster_ensure(chat_id)
    with _conn() as con:
        con.execute("""
            insert or ignore into cluster_members (user_id, chat_id)
            values (?, ?)
        """, (uid, chat_id))


def cluster_leave(uid):
    with _conn() as con:
        con.execute(
            "delete from cluster_members where user_id=?", (uid,)
        )


def user_cluster(uid):
    with _conn() as con:
        row = con.execute("""
            select cm.chat_id from cluster_members cm
            where cm.user_id=?
        """, (uid,)).fetchone()
        return row["chat_id"] if row else None


def cluster_members(chat_id):
    with _conn() as con:
        return con.execute("""
            select u.* from users u
            join cluster_members cm on cm.user_id=u.user_id
            where cm.chat_id=?
        """, (chat_id,)).fetchall()


def cluster_info(chat_id):
    with _conn() as con:
        return con.execute(
            "select * from clusters where chat_id=?", (chat_id,)
        ).fetchone()


def update_cluster(chat_id, **kwargs):
    if not kwargs:
        return
    sets = ", ".join(f"{k}=?" for k in kwargs)
    vals = list(kwargs.values()) + [chat_id]
    with _conn() as con:
        con.execute(f"update clusters set {sets} where chat_id=?", vals)
