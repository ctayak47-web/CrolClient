import os
import sqlite3

DB_PATH = os.environ.get("REGBOT_DB_PATH", "data/reg_users.db")

_conn = None


def _get_conn():
    global _conn
    if _conn is None:
        os.makedirs(os.path.dirname(DB_PATH) or ".", exist_ok=True)
        _conn = sqlite3.connect(DB_PATH, check_same_thread=False)
        _conn.execute('''
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY,
                username TEXT,
                first_interaction TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                registration_date TEXT,
                calculated_timestamp INTEGER
            )
        ''')
        _conn.commit()
    return _conn


def register_user(user_id: int, username: str = None, reg_date: str = None, timestamp: int = None):
    con = _get_conn()
    con.execute(
        "INSERT OR IGNORE INTO users (user_id, username, registration_date, calculated_timestamp) VALUES (?, ?, ?, ?)",
        (user_id, username, reg_date, timestamp),
    )
    con.commit()


def get_stats() -> int:
    con = _get_conn()
    cur = con.execute("SELECT COUNT(*) FROM users")
    return cur.fetchone()[0]


def all_user_ids():
    con = _get_conn()
    cur = con.execute("SELECT user_id FROM users")
    return [row[0] for row in cur.fetchall()]
