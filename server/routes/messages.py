from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status, Depends
from sqlmodel import SQLModel

from services.messages import (
    create_message,
    list_messages,
    get_message,
    update_message,
    delete_message,
)
from models import Message as MessageModel
from models import User as UserModel
from dependencies.token_verification import verify_jwt

router = APIRouter(
    prefix="/messages",
    tags=["Messages"],
    dependencies=[Depends(verify_jwt)],
)

class MessageCreate(SQLModel):
    sender_id: int
    receiver_id: int
    content: str
    timestamp: datetime | None = None
    read: bool | None = False

class MessageUpdate(SQLModel):
    content: str | None = None
    read: bool | None = None


@router.get("", response_model=List[MessageModel])
async def read_messages(
    current_user: UserModel = Depends(verify_jwt)
):
    return list_messages()


@router.post("", response_model=MessageModel, status_code=status.HTTP_201_CREATED)
async def create(
    msg: MessageCreate,
    current_user: UserModel = Depends(verify_jwt)
):
    data = msg.dict(exclude_unset=True)
    if data.get("timestamp") is None:
        data["timestamp"] = datetime.utcnow()
    return create_message(MessageModel(**data))


@router.get("/{message_id}", response_model=MessageModel)
async def read_message(
    message_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    m = get_message(message_id)
    if not m:
        raise HTTPException(status_code=404, detail="Message not found")
    return m


@router.put("/{message_id}", response_model=MessageModel)
async def replace_message(
    message_id: int,
    msg: MessageUpdate,
    current_user: UserModel = Depends(verify_jwt)
):
    updated = update_message(message_id, msg.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Message not found")
    return updated


@router.delete("/{message_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_message(
    message_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    if not delete_message(message_id):
        raise HTTPException(status_code=404, detail="Message not found")
