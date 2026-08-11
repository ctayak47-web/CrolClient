import os
import sqlite3
import threading
from typing import List, Optional, Tuple

DB_PATH = os.environ.get("REGBOT_DB_PATH", "data/reg_users.db")

_conn = None
_lock = threading.RLock()


def _get_conn() -> sqlite3.Connection:
    global _conn
    with _lock:
        if _conn is None:
            os.makedirs(os.path.dirname(DB_PATH) or ".", exist_ok=True)
            _conn = sqlite3.connect(
                DB_PATH,
                check_same_thread=False,
                timeout=2.0,
            )
            _conn.execute("PRAGMA journal_mode=WAL")
            _conn.execute("PRAGMA synchronous=NORMAL")
            _conn.execute("PRAGMA busy_timeout=2000")
            _conn.execute(
                """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY,
                    username TEXT,
                    first_interaction TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    registration_date TEXT,
                    calculated_timestamp INTEGER
                )
                """
            )
            _conn.execute(
                """
                CREATE TABLE IF NOT EXISTS analysis_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    chat_id INTEGER NOT NULL,
                    target_id INTEGER NOT NULL,
                    username TEXT,
                    registration_date TEXT NOT NULL,
                    calculated_timestamp INTEGER NOT NULL,
                    result_text TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """
            )
            _conn.execute(
                """
                CREATE INDEX IF NOT EXISTS idx_analysis_history_chat
                ON analysis_history(chat_id, id)
                """
            )
            _conn.commit()
        return _conn


def register_user(
    user_id: int,
    username: Optional[str] = None,
    reg_date: Optional[str] = None,
    timestamp: Optional[int] = None,
) -> None:
    con = _get_conn()
    with _lock:
        con.execute(
            """
            INSERT INTO users
                (user_id, username, registration_date, calculated_timestamp)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(user_id) DO UPDATE SET
                username = COALESCE(excluded.username, users.username),
                registration_date = COALESCE(excluded.registration_date, users.registration_date),
                calculated_timestamp = COALESCE(excluded.calculated_timestamp, users.calculated_timestamp)
            """,
            (user_id, username, reg_date, timestamp),
        )
        con.commit()


def save_analysis(
    chat_id: int,
    target_id: int,
    username: Optional[str],
    registration_date: str,
    calculated_timestamp: int,
    result_text: str,
) -> None:
    con = _get_conn()
    with _lock:
        con.execute(
            """
            INSERT INTO analysis_history
                (chat_id, target_id, username, registration_date,
                 calculated_timestamp, result_text)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            (
                chat_id,
                target_id,
                username,
                registration_date,
                calculated_timestamp,
                result_text,
            ),
        )
        con.commit()


def get_stats() -> int:
    con = _get_conn()
    with _lock:
        cur = con.execute("SELECT COUNT(*) FROM users")
        row = cur.fetchone()
        return int(row[0] if row else 0)


def all_user_ids() -> List[int]:
    con = _get_conn()
    with _lock:
        cur = con.execute("SELECT user_id FROM users")
        return [int(row[0]) for row in cur.fetchall()]


def close() -> None:
    global _conn
    with _lock:
        if _conn is not None:
            _conn.close()
            _conn = None
