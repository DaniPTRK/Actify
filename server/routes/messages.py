from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status
from sqlmodel import SQLModel

from services.messages import (
    create_message,
    list_messages,
    get_message,
    update_message,
    delete_message,
)
from models import Message as MessageModel

router = APIRouter(prefix="/messages", tags=["Messages"])

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
async def read_messages():
    return list_messages()

@router.post("", response_model=MessageModel, status_code=status.HTTP_201_CREATED)
async def create(msg: MessageCreate):
    data = msg.dict(exclude_unset=True)
    if data.get("timestamp") is None:
        data["timestamp"] = datetime.utcnow()
    return create_message(MessageModel(**data))

@router.get("/{message_id}", response_model=MessageModel)
async def read_message(message_id: int):
    m = get_message(message_id)
    if not m:
        raise HTTPException(status_code=404, detail="Message not found")
    return m

@router.put("/{message_id}", response_model=MessageModel)
async def replace_message(message_id: int, msg: MessageUpdate):
    updated = update_message(message_id, msg.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Message not found")
    return updated

@router.delete("/{message_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_message(message_id: int):
    if not delete_message(message_id):
        raise HTTPException(status_code=404, detail="Message not found")
