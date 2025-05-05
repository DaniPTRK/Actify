from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status
from sqlmodel import SQLModel

from services.likes import (
    create_like,
    list_likes,
    get_like,
    update_like,
    delete_like,
)
from models import Like as LikeModel

router = APIRouter(prefix="/likes", tags=["Likes"])

class LikeCreate(SQLModel):
    user_id: int
    post_id: int | None = None
    comment_id: int | None = None
    created_at: datetime | None = None

class LikeUpdate(SQLModel):
    # Only timestamp can realistically change (e.g. back-date), but normally you’d delete instead
    created_at: datetime | None = None

@router.get("", response_model=List[LikeModel])
async def read_likes():
    return list_likes()

@router.post("", response_model=LikeModel, status_code=status.HTTP_201_CREATED)
async def create(lk: LikeCreate):
    data = lk.dict(exclude_unset=True)
    if data.get("created_at") is None:
        data["created_at"] = datetime.utcnow()
    return create_like(LikeModel(**data))

@router.get("/{like_id}", response_model=LikeModel)
async def read_like(like_id: int):
    l = get_like(like_id)
    if not l:
        raise HTTPException(status_code=404, detail="Like not found")
    return l

@router.put("/{like_id}", response_model=LikeModel)
async def replace_like(like_id: int, lk: LikeUpdate):
    updated = update_like(like_id, lk.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Like not found")
    return updated

@router.delete("/{like_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_like(like_id: int):
    if not delete_like(like_id):
        raise HTTPException(status_code=404, detail="Like not found")
