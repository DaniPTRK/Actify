from typing import List, Optional
from sqlmodel import select
from fastapi import HTTPException, status

from db import get_session
from models import Users


def create_user(payload: Users) -> Users:

    with get_session() as session:
        
        existing = session.exec(
            select(Users).where(Users.email == payload.email)
        ).first()

        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="User already exists",
            )

        new_user = Users(**payload.dict())
        session.add(new_user)
        session.flush()
        session.refresh(new_user)

        return new_user


def list_users() -> List[Users]:
    with get_session() as session:
        return session.exec(select(Users)).all()


def get_user(user_id: int) -> Optional[Users]:
    with get_session() as session:
        return session.get(Users, user_id)


def update_user(user_id: int, updates: dict) -> Optional[Users]:
    with get_session() as session:
        user = session.get(Users, user_id)
        if not user:
            return None
        for key, val in updates.items():
            setattr(user, key, val)
        session.add(user)
        return user


def delete_user(user_id: int) -> bool:
    with get_session() as session:
        user = session.get(Users, user_id)
        if not user:
            return False
        session.delete(user)
        return True
