from typing import List
from fastapi import APIRouter, HTTPException, status
from sqlmodel import SQLModel

from services.xp_badges import (
    create_badge,
    list_badges,
    get_badge,
    update_badge,
    delete_badge,
)
from models import XPBadge as XPBadgeModel

router = APIRouter(prefix="/xp-badges", tags=["XP Badges"])


class XPBadgeCreate(SQLModel):
    user_id: int
    xp_points: int
    badge_name: str


class XPBadgeUpdate(SQLModel):
    xp_points: int | None = None
    badge_name: str | None = None


@router.get("", response_model=List[XPBadgeModel])
async def read_badges():
    return list_badges()


@router.post("", response_model=XPBadgeModel, status_code=status.HTTP_201_CREATED)
async def create(badge: XPBadgeCreate):
    return create_badge(XPBadgeModel(**badge.dict()))


@router.get("/{record_id}", response_model=XPBadgeModel)
async def read_badge(record_id: int):
    b = get_badge(record_id)
    if not b:
        raise HTTPException(status_code=404, detail="Badge not found")
    return b


@router.put("/{record_id}", response_model=XPBadgeModel)
async def replace_badge(record_id: int, badge: XPBadgeUpdate):
    updated = update_badge(record_id, badge.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Badge not found")
    return updated


@router.delete("/{record_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_badge(record_id: int):
    if not delete_badge(record_id):
        raise HTTPException(status_code=404, detail="Badge not found")
