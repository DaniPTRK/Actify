from typing import List, Optional
from sqlmodel import select

from db import get_session
from models import XPBadge


def create_badge(payload: XPBadge) -> XPBadge:
    with get_session() as session:
        session.add(payload)
        session.flush()        # assign PK (needed before refresh in MySQL)
        session.refresh(payload)
        return payload


def list_badges() -> List[XPBadge]:
    with get_session() as session:
        return session.exec(select(XPBadge)).all()


def get_badge(record_id: int) -> Optional[XPBadge]:
    with get_session() as session:
        return session.get(XPBadge, record_id)


def update_badge(record_id: int, updates: dict) -> Optional[XPBadge]:
    with get_session() as session:
        badge = session.get(XPBadge, record_id)
        if not badge:
            return None
        for k, v in updates.items():
            setattr(badge, k, v)
        session.add(badge)
        return badge


def delete_badge(record_id: int) -> bool:
    with get_session() as session:
        badge = session.get(XPBadge, record_id)
        if not badge:
            return False
        session.delete(badge)
        return True
