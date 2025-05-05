from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status
from sqlmodel import SQLModel

from services.recipes import (
    create_recipe,
    list_recipes,
    get_recipe,
    update_recipe,
    delete_recipe,
)
from models import Recipe as RecipeModel

router = APIRouter(prefix="/recipes", tags=["Recipes"])

class RecipeCreate(SQLModel):
    title: str
    description: str
    ingredients: str
    steps: str
    author_id: int
    created_at: datetime | None = None

class RecipeUpdate(SQLModel):
    title: str | None = None
    description: str | None = None
    ingredients: str | None = None
    steps: str | None = None

@router.get("", response_model=List[RecipeModel])
async def read_recipes():
    return list_recipes()

@router.post("", response_model=RecipeModel, status_code=status.HTTP_201_CREATED)
async def create(rec: RecipeCreate):
    data = rec.dict(exclude_unset=True)
    if data.get("created_at") is None:
        data["created_at"] = datetime.utcnow()
    return create_recipe(RecipeModel(**data))

@router.get("/{recipe_id}", response_model=RecipeModel)
async def read_recipe(recipe_id: int):
    r = get_recipe(recipe_id)
    if not r:
        raise HTTPException(status_code=404, detail="Recipe not found")
    return r

@router.put("/{recipe_id}", response_model=RecipeModel)
async def replace_recipe(recipe_id: int, rec: RecipeUpdate):
    updated = update_recipe(recipe_id, rec.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Recipe not found")
    return updated

@router.delete("/{recipe_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_recipe(recipe_id: int):
    if not delete_recipe(recipe_id):
        raise HTTPException(status_code=404, detail="Recipe not found")
