from typing import List, Optional
from sqlmodel import select
from db import get_session
from models import Notification
from models import User as UserModel


def create_notification(payload: Notification) -> Notification:
    with get_session() as session:
        session.add(payload)
        session.flush()
        session.refresh(payload)
        return payload

"""
    Returns only the notifications owned by current_user
"""
def list_notifications(current_user: UserModel) -> List[Notification]:
    with get_session() as session:
        stmt = (
            select(Notification)
            .where(Notification.user_id == current_user.id)
            .order_by(Notification.created_at.desc())
        )
        return session.exec(stmt).all()

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
