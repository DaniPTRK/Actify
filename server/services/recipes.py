from typing import List, Optional
from sqlmodel import select
from db import get_session
from models import Recipe

def create_recipe(payload: Recipe) -> Recipe:
    with get_session() as session:
        session.add(payload)
        session.flush()
        session.refresh(payload)
        return payload

def list_recipes() -> List[Recipe]:
    with get_session() as session:
        return session.exec(select(Recipe)).all()

def get_recipe(recipe_id: int) -> Optional[Recipe]:
    with get_session() as session:
        return session.get(Recipe, recipe_id)

def update_recipe(recipe_id: int, updates: dict) -> Optional[Recipe]:
    with get_session() as session:
        rec = session.get(Recipe, recipe_id)
        if not rec:
            return None
        for k, v in updates.items():
            setattr(rec, k, v)
        session.add(rec)
        return rec

def delete_recipe(recipe_id: int) -> bool:
    with get_session() as session:
        rec = session.get(Recipe, recipe_id)
        if not rec:
            return False
        session.delete(rec)
        return True
