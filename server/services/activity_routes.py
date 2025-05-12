from typing import List, Optional
from sqlmodel import select

from db import get_session
from models import ActivityRoute


def create_route(payload: ActivityRoute) -> ActivityRoute:
    with get_session() as session:
        session.add(payload)
        session.flush()
        session.refresh(payload)
        return payload


def list_routes() -> List[ActivityRoute]:
    with get_session() as session:
        return session.exec(select(ActivityRoute)).all()


def get_route(route_id: int) -> Optional[ActivityRoute]:
    with get_session() as session:
        return session.get(ActivityRoute, route_id)


def update_route(route_id: int, updates: dict) -> Optional[ActivityRoute]:
    with get_session() as session:
        route = session.get(ActivityRoute, route_id)
        if not route:
            return None
        for key, val in updates.items():
            setattr(route, key, val)
        session.add(route)
        return route


def delete_route(route_id: int) -> bool:
    with get_session() as session:
        route = session.get(ActivityRoute, route_id)
        if not route:
            return False
        session.delete(route)
        return True
