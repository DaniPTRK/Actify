# routes/comments.py
from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status
from sqlmodel import SQLModel

from services.comments import (
    create_comment,
    list_comments,
    get_comment,
    update_comment,
    delete_comment,
)
from models import Comment as CommentModel

router = APIRouter(prefix="/comments", tags=["Comments"])


class CommentCreate(SQLModel):
    post_id: int
    user_id: int
    content: str
    timestamp: datetime | None = None


class CommentUpdate(SQLModel):
    content: str | None = None


@router.get("", response_model=List[CommentModel])
async def read_comments():
    return list_comments()


@router.post("", response_model=CommentModel, status_code=status.HTTP_201_CREATED)
async def create(c: CommentCreate):
    data = c.dict(exclude_unset=True)
    if data.get("timestamp") is None:
        from datetime import datetime
        data["timestamp"] = datetime.utcnow()
    return create_comment(CommentModel(**data))


@router.get("/{comment_id}", response_model=CommentModel)
async def read_comment(comment_id: int):
    c = get_comment(comment_id)
    if not c:
        raise HTTPException(status_code=404, detail="Comment not found")
    return c


@router.put("/{comment_id}", response_model=CommentModel)
async def replace_comment(comment_id: int, c: CommentUpdate):
    updated = update_comment(comment_id, c.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Comment not found")
    return updated


@router.delete("/{comment_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_comment(comment_id: int):
    if not delete_comment(comment_id):
        raise HTTPException(status_code=404, detail="Comment not found")
