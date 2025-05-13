from typing import List, Optional
from sqlmodel import select
from fastapi import HTTPException, status

from db import get_session
from models import Users, UserProfiles


def create_user_profile(payload: UserProfiles) -> Users:

    with get_session() as session:
        
        existing = session.exec(
            select(UserProfiles).where(UserProfiles.user_id == payload.user_id)
        ).first()

        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="User profile already exists",
            )

        new_user = payload
        session.add(new_user)
        session.flush()
        session.refresh(new_user)

        return new_user
    
def get_user_profile_by_user_id(user_id: int) -> Optional[UserProfiles]:
    with get_session() as session:
        stmt = select(UserProfiles).where(UserProfiles.user_id == user_id)
        return session.exec(stmt).one_or_none()


def update_user_profile(user_profile: UserProfiles, updates: dict) -> Optional[UserProfiles]:
    with get_session() as session:

        for key, val in updates.items():
            setattr(user_profile, key, val)

        session.add(user_profile)
        return user_profile
