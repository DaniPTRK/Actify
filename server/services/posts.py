from typing import List, Optional
from sqlmodel import select

from db import get_session
from models import Post


def create_post(payload: Post) -> Post:
    with get_session() as session:
        session.add(payload)
        session.flush()
        session.refresh(payload)
        return payload


def list_posts() -> List[Post]:
    with get_session() as session:
        return session.exec(select(Post)).all()


def get_post(post_id: int) -> Optional[Post]:
    with get_session() as session:
        return session.get(Post, post_id)


def update_post(post_id: int, updates: dict) -> Optional[Post]:
    with get_session() as session:
        post = session.get(Post, post_id)
        if not post:
            return None
        for key, val in updates.items():
            setattr(post, key, val)
        session.add(post)
        return post


def delete_post(post_id: int) -> bool:
    with get_session() as session:
        post = session.get(Post, post_id)
        if not post:
            return False
        session.delete(post)
        return True
