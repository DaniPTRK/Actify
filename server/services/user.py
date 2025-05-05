from typing import List, Optional
from sqlmodel import select

from db import get_session
from models import User


def create_user(payload: User) -> User:
    with get_session() as session:
        session.add(payload)
        session.flush()
        session.refresh(payload)
        return payload


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
