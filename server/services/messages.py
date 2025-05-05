from typing import List, Optional
from sqlmodel import select
from db import get_session
from models import Message

def create_message(payload: Message) -> Message:
    with get_session() as session:
        session.add(payload)
        session.flush()
        session.refresh(payload)
        return payload

def list_messages() -> List[Message]:
    with get_session() as session:
        return session.exec(select(Message)).all()

def get_message(message_id: int) -> Optional[Message]:
    with get_session() as session:
        return session.get(Message, message_id)

def update_message(message_id: int, updates: dict) -> Optional[Message]:
    with get_session() as session:
        msg = session.get(Message, message_id)
        if not msg:
            return None
        for k, v in updates.items():
            setattr(msg, k, v)
        session.add(msg)
        return msg

def delete_message(message_id: int) -> bool:
    with get_session() as session:
        msg = session.get(Message, message_id)
        if not msg:
            return False
        session.delete(msg)
        return True
