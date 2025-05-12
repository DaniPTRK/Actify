from typing import List, Optional
from sqlmodel import select

from db import get_session
from models import Post, Users
from fastapi import HTTPException, status
from logic.posts_logic import can_delete_post


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


def update_post(post_id: int, updates: dict) -> Optional[Post]:
    with get_session() as session:
        post = session.get(Post, post_id)
        if not post:
            return None
        for key, val in updates.items():
            setattr(post, key, val)
        session.add(post)
        return post


def delete_post(post_id: int, loggedUser: Users) -> bool:
    with get_session() as session:

        post = session.get(Post, post_id)

        if not post:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="There is no post with the provided post_id",
            )

        
        # check if the user can delete the post
        if not can_delete_post(loggedUser, post):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="You dont have the permission to delete this post",
            )

        session.delete(post)
        return True
