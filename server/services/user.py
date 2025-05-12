from typing import List, Optional
from sqlmodel import select

from db import get_session
from models import User
from CRUD import create_user as CRUD_create_user


def create_user(payload: User) -> User:
    return CRUD_create_user(**payload.dict())


def list_users() -> List[User]:
    with get_session() as session:
        return session.exec(select(User)).all()


def get_user(user_id: int) -> Optional[User]:
    with get_session() as session:
        return session.get(User, user_id)


def update_user(user_id: int, updates: dict) -> Optional[User]:
    with get_session() as session:
        user = session.get(User, user_id)
        if not user:
            return None
        for key, val in updates.items():
            setattr(user, key, val)
        session.add(user)
        return user


def delete_user(user_id: int) -> bool:
    with get_session() as session:
        user = session.get(User, user_id)
        if not user:
            return False
        session.delete(user)
        return True
