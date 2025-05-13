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


def get_user_by_id(user_id: int) -> Optional[Users]:
    with get_session() as session:
        return session.get(Users, user_id)


def get_user_by_email(email: str) -> Optional[Users]:
    with get_session() as session:
        stmt = select(Users).where(Users.email == email)
        return session.exec(stmt).one_or_none()


def get_users_by_name(prefix: str) -> List[Users]:
    with get_session() as session:
        stmt = select(Users).where(Users.name.ilike(f"{prefix}%"))
        return session.exec(stmt).all()


def update_user(user: Users, updates: dict) -> Optional[Users]:
    with get_session() as session:

        for key, val in updates.items():
            setattr(user, key, val)

        session.add(user)
        return user


def delete_user(user: Users) -> bool:
    with get_session() as session:

        session.delete(user)
        return True
