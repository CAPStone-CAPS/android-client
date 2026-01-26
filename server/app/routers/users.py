from fastapi import APIRouter

router = APIRouter(prefix="/users", tags=["users"])

FAKE_USERS = [
    {"id": 1, "name": "Alex"},
    {"id": 2, "name": "Min"},
]


@router.get("")
def list_users():
    return FAKE_USERS
