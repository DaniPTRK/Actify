from typing import List, Optional
from sqlmodel import select

from db import get_session
from models import Post, Users
from fastapi import HTTPException, status


'''only the logged user can create a post'''
def create_post(payload: Post, loggedUser: Users) -> Post:
    with get_session() as session:
        
        if loggedUser.user_id != payload.user_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="The provided user_id is different from the logged in user",
            )
            

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


def update_post(post: Post, updates: dict) -> Optional[Post]:
    with get_session() as session:

        for key, val in updates.items():
            setattr(post, key, val)

        session.add(post)
        return post


def delete_post(post: Post, loggedUser: Users) -> bool:
    with get_session() as session:

        session.delete(post)
        return True
