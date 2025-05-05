from typing import List, Optional
from sqlmodel import select
from db import get_session
from models import Notification

def create_notification(payload: Notification) -> Notification:
    with get_session() as session:
        session.add(payload)
        session.flush()
        session.refresh(payload)
        return payload

def list_notifications() -> List[Notification]:
    with get_session() as session:
        return session.exec(select(Notification)).all()

def get_notification(notification_id: int) -> Optional[Notification]:
    with get_session() as session:
        return session.get(Notification, notification_id)

def update_notification(notification_id: int, updates: dict) -> Optional[Notification]:
    with get_session() as session:
        notif = session.get(Notification, notification_id)
        if not notif:
            return None
        for k, v in updates.items():
            setattr(notif, k, v)
        session.add(notif)
        return notif

def delete_notification(notification_id: int) -> bool:
    with get_session() as session:
        notif = session.get(Notification, notification_id)
        if not notif:
            return False
        session.delete(notif)
        return True
