from typing import List, Optional
from sqlmodel import select

from db import get_session
from models import Comment


def create_comment(payload: Comment) -> Comment:
    with get_session() as session:
        session.add(payload)
        session.flush()
        session.refresh(payload)
        return payload


def list_comments() -> List[Comment]:
    with get_session() as session:
        return session.exec(select(Comment)).all()


def get_comment(comment_id: int) -> Optional[Comment]:
    with get_session() as session:
        return session.get(Comment, comment_id)


def update_comment(comment_id: int, updates: dict) -> Optional[Comment]:
    with get_session() as session:
        comment = session.get(Comment, comment_id)
        if not comment:
            return None
        for key, val in updates.items():
            setattr(comment, key, val)
        session.add(comment)
        return comment


def delete_comment(comment_id: int) -> bool:
    with get_session() as session:
        comment = session.get(Comment, comment_id)
        if not comment:
            return False
        session.delete(comment)
        return True