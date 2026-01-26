from fastapi import APIRouter

from ..db import check_db

router = APIRouter()


@router.get("/health")
def health():
    db_status = check_db()
    return {"status": "ok", "db": db_status}
