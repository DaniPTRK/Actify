from typing import List, Optional
from sqlmodel import select

from db import get_session
from models import Event


def create_event(payload: Event) -> Event:
    with get_session() as session:
        session.add(payload)
        session.flush()
        session.refresh(payload)
        return payload


def list_events() -> List[Event]:
    with get_session() as session:
        return session.exec(select(Event)).all()


def get_event(event_id: int) -> Optional[Event]:
    with get_session() as session:
        return session.get(Event, event_id)


def update_event(event_id: int, updates: dict) -> Optional[Event]:
    with get_session() as session:
        event = session.get(Event, event_id)
        if not event:
            return None
        for key, val in updates.items():
            setattr(event, key, val)
        session.add(event)
        return event


def delete_event(event_id: int) -> bool:
    with get_session() as session:
        event = session.get(Event, event_id)
        if not event:
            return False
        session.delete(event)
        return True
