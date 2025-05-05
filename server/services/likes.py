from typing import List, Optional
from sqlmodel import select
from db import get_session
from models import Like

def create_like(payload: Like) -> Like:
    with get_session() as session:
        session.add(payload)
        session.flush()
        session.refresh(payload)
        return payload

def list_likes() -> List[Like]:
    with get_session() as session:
        return session.exec(select(Like)).all()

def get_like(like_id: int) -> Optional[Like]:
    with get_session() as session:
        return session.get(Like, like_id)

def update_like(like_id: int, updates: dict) -> Optional[Like]:
    with get_session() as session:
        lk = session.get(Like, like_id)
        if not lk:
            return None
        for k, v in updates.items():
            setattr(lk, k, v)
        session.add(lk)
        return lk

def delete_like(like_id: int) -> bool:
    with get_session() as session:
        lk = session.get(Like, like_id)
        if not lk:
            return False
        session.delete(lk)
        return True
