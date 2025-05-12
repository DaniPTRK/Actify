# routes/posts.py
from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status, Depends
from sqlmodel import SQLModel

from services.posts import (
    create_post,
    list_posts,
    get_post,
    update_post,
    delete_post,
)
from models import Post as PostModel
from models import Users as UserModel
from dependencies.token_verification import verify_jwt

router = APIRouter(
    prefix="/posts",
    tags=["Posts"],
    dependencies=[Depends(verify_jwt)]
)


class PostCreate(SQLModel):
    user_id: int
    content: str
    media_url: str | None = None
    timestamp: datetime | None = None


class PostUpdate(SQLModel):
    content: str | None = None
    media_url: str | None = None


@router.get("", response_model=List[PostModel])
async def read_posts(
    current_user: UserModel = Depends(verify_jwt)
):
    return list_posts()


@router.post("", response_model=PostModel, status_code=status.HTTP_201_CREATED)
async def create(
    p: PostCreate,
    current_user: UserModel = Depends(verify_jwt)
):
    data = p.dict(exclude_unset=True)
    if data.get("timestamp") is None:
        data["timestamp"] = datetime.utcnow()

    return create_post(PostModel(**data), current_user)


@router.get("/{post_id}", response_model=PostModel)
async def read_post(
    post_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    p = get_post(post_id)
    if not p:
        raise HTTPException(status_code=404, detail="Post not found")
    return p


@router.put("/{post_id}", response_model=PostModel)
async def replace_post(
    post_id: int,
    p: PostUpdate,
    current_user: UserModel = Depends(verify_jwt)
):
    updated = update_post(post_id, p.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Post not found")
    return updated


@router.delete("/{post_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_post(
    post_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    if not delete_post(post_id, current_user):
        raise HTTPException(status_code=404, detail="Post not found")
