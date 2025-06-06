# routes/events.py
from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status, Depends
from sqlmodel import SQLModel

from services.events import (
    create_event,
    list_events,
    get_event,
    update_event,
    delete_event,
)
from models import Event as EventModel
from models import Users as UserModel
from dependencies.token_verification import verify_jwt

router = APIRouter(
    prefix="/events",
    tags=["Events"],
    dependencies=[Depends(verify_jwt)]
)


class EventCreate(SQLModel):
    title: str
    description: str
    location: str
    date_time: datetime
    max_participants: int


class EventUpdate(SQLModel):
    title: str | None = None
    description: str | None = None
    location: str | None = None
    date_time: datetime | None = None
    max_participants: int | None = None


@router.get("/", response_model=List[EventModel])
async def read_events(
    current_user: UserModel = Depends(verify_jwt)
):
    return list_events()


@router.post("/", response_model=EventModel, status_code=status.HTTP_201_CREATED)
async def create(
    e: EventCreate,
    current_user: UserModel = Depends(verify_jwt)
):
    return create_event(EventModel(**e.dict()))


@router.get("/{event_id}/", response_model=EventModel)
async def read_event(
    event_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    ev = get_event(event_id)
    if not ev:
        raise HTTPException(status_code=404, detail="Event not found")
    return ev


@router.put("/{event_id}/", response_model=EventModel)
async def replace_event(
    event_id: int,
    e: EventUpdate,
    current_user: UserModel = Depends(verify_jwt)
):
    updated = update_event(event_id, e.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Event not found")
    return updated


@router.delete("/{event_id}/", status_code=status.HTTP_204_NO_CONTENT)
async def remove_event(
    event_id: int,
    current_user: UserModel = Depends(verify_jwt),
):
    if not delete_event(event_id):
        raise HTTPException(status_code=404, detail="Event not found")
