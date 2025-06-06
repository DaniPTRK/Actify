from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status, Depends
from sqlmodel import SQLModel
from services.notifications import (
    create_notification,
    list_notifications,
    get_notification,
    update_notification,
    delete_notification,
)
from models import Notification as NotificationModel
from models import Users as UserModel
from dependencies.token_verification import verify_jwt, decode_and_get_user
from fastapi import WebSocket, WebSocketDisconnect, status
from dependencies.token_verification import SECRET_KEY, ALGORITHM
from logic.notifications_logic import *


router = APIRouter(
    prefix="/notifications",
    tags=["Notifications"],
    dependencies=[Depends(verify_jwt)],
)

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
async def read_notifications(
    current_user: UserModel = Depends(verify_jwt)
):
    return list_notifications(current_user)


@router.post("", response_model=NotificationModel, status_code=status.HTTP_201_CREATED)
async def create_notification_route(
    notif: NotificationCreate,
    current_user: UserModel = Depends(verify_jwt)
):
    data = notif.dict(exclude_unset=True)

    if data.get("created_at") is None:
        data["created_at"] = datetime.utcnow()
        
    new_notif = create_notification(NotificationModel(**data))

    return new_notif



@router.get("/{notification_id}", response_model=NotificationModel)
async def read_notification(
    notification_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    n = get_notification(notification_id)
    if not n:
        raise HTTPException(status_code=404, detail="Notification not found")
    return n



@router.put("/{notification_id}", response_model=NotificationModel)
async def replace_notification(
    notification_id: int,
    notif: NotificationUpdate,
    current_user: UserModel = Depends(verify_jwt)
):
    updated = update_notification(notification_id, notif.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Notification not found")
    return updated


'''a user can delete the notifications that are for him'''
@router.delete("/{notification_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_notification(
    notification_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    notification = get_notification(notification_id)
    
    if can_delete_notification(current_user, notification) == False:
        raise HTTPException(status_code=401, detail="You dont have permission to delete the notification.")
        
    if not delete_notification(notification_id, current_user):
        raise HTTPException(status_code=404, detail="Notification not found")
