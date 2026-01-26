import contextlib
import os

import psycopg2


def check_db() -> str:
    dsn = os.getenv("DATABASE_URL")
    if not dsn:
        return "disabled"

    try:
        with contextlib.closing(psycopg2.connect(dsn, connect_timeout=3)) as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT 1")
        return "ok"
    except Exception as exc:
        return f"error: {type(exc).__name__}"
