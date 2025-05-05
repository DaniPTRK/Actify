from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status
from sqlmodel import SQLModel
from services.notifications import (
    create_notification,
    list_notifications,
    get_notification,
    update_notification,
    delete_notification,
)
from models import Notification as NotificationModel

router = APIRouter(prefix="/notifications", tags=["Notifications"])

class NotificationCreate(SQLModel):
    user_id: int
    content: str
    type: str | None = None
    created_at: datetime | None = None
    read: bool | None = False

class NotificationUpdate(SQLModel):
    content: str | None = None
    type: str | None = None
    read: bool | None = None

@router.get("", response_model=List[NotificationModel])
async def read_notifications():
    return list_notifications()

@router.post("", response_model=NotificationModel, status_code=status.HTTP_201_CREATED)
async def create(notif: NotificationCreate):
    data = notif.dict(exclude_unset=True)
    if data.get("created_at") is None:
        data["created_at"] = datetime.utcnow()
    return create_notification(NotificationModel(**data))

@router.get("/{notification_id}", response_model=NotificationModel)
async def read_notification(notification_id: int):
    n = get_notification(notification_id)
    if not n:
        raise HTTPException(status_code=404, detail="Notification not found")
    return n

@router.put("/{notification_id}", response_model=NotificationModel)
async def replace_notification(notification_id: int, notif: NotificationUpdate):
    updated = update_notification(notification_id, notif.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Notification not found")
    return updated

@router.delete("/{notification_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_notification(notification_id: int):
    if not delete_notification(notification_id):
        raise HTTPException(status_code=404, detail="Notification not found")
