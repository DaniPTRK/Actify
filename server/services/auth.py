from fastapi import HTTPException, status
from sqlmodel import select

from db import get_session
from models import Users

from routes.user import UserCreate

from services.user import create_user


def register_user(input: UserCreate) -> Users:
    return create_user(input)


def authenticate_user(email: str, password_hash: str) -> Users:
    """Verify credentials or raise 401."""

    with get_session() as session:
        user = session.exec(
            select(Users).where(Users.email == email)
        ).first()
        

        if not user or user.password_hash != password_hash:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid credentials",
            )


        return user
