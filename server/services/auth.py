from typing import Optional
from fastapi import HTTPException, status
from sqlmodel import select

from db import get_session
from models import User


def register_user(username: str, password: str) -> User:
    """Register a new user or raise 400 if the username is taken."""

    with get_session() as session:
        # Check if username exists
        existing = session.exec(
            select(User).where(User.username == username)
        ).first()
        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="User already exists",
            )
        # Create user (password stored as hash in production!)
        new_user = User(username=username, password_hash=password)
        session.add(new_user)
        session.flush()
        session.refresh(new_user)
        return new_user


def authenticate_user(username: str, password: str) -> User:
    """Verify credentials or raise 401."""
    with get_session() as session:
        user = session.exec(
            select(User).where(User.username == username)
        ).first()
        if not user or user.password_hash != password:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid credentials",
            )
        return user
