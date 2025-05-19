from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status, Depends
from sqlmodel import SQLModel, Field
from typing import Optional, List
from models import Recipe
import sys
import os

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "../..")))

from services.recipes import (
    create_recipe,
    list_recipes,
    get_recipe,
    delete_recipe,
    get_recipes_by_user_id
)
from models import Recipe as RecipeModel
from models import Users as UserModel
from dependencies.token_verification import verify_jwt
from RecipeRecommender.RecipeRecommender import recommend_api

router = APIRouter(
    prefix="/recipes",
    tags=["Recipes"],
    dependencies=[Depends(verify_jwt)],
)


class RecommendRequest(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    user_text: str
    allergens: Optional[str] = []
    diet: str
    dish_category: str
    time_min: int = 10
    time_max: int = 500

class RecommendResponse(SQLModel, table=True):
    recipe_id: Optional[int] = Field(default=None, primary_key=True)
    image: str
    name: str
    user_input: str
    diet_type: str
    allergens: Optional[str]
    total_time: int
    ingredients: Optional[str]
    site: str
    calories: float | None = None

@router.get("/", response_model=List[RecipeModel])
async def read_recipes(
    current_user: UserModel = Depends(verify_jwt)
):
    return list_recipes()


@router.get("/{recipe_id}/", response_model=RecipeModel)
async def read_recipe(
    recipe_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    r = get_recipe(recipe_id)
    if not r:
        raise HTTPException(status_code=404, detail="Recipe not found")
    return r


@router.get("/user_id/{user_id}/", response_model=List[RecipeModel])
async def read_recipes_by_user_id(
    user_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    r = get_recipes_by_user_id(user_id)

    if not r:
        raise HTTPException(status_code=404, detail="Recipe not found")

    return r


@router.delete("/{recipe_id}/", status_code=status.HTTP_204_NO_CONTENT)
async def remove_recipe(
    recipe_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    if not delete_recipe(recipe_id):
        raise HTTPException(status_code=404, detail="Recipe not found")




@router.post("/recommend/",
             response_model=RecommendResponse,
             status_code=status.HTTP_200_OK,
             include_in_schema=True)
async def recommend_endpoint(payload: RecommendRequest,
                             current_user: UserModel = Depends(verify_jwt)):

    result = recommend_api(payload.dict())

    if isinstance(result, dict) and result.get("error"):
        raise HTTPException(status_code=406, detail=result.get("error", "Recommendation failed."))

    data = result
    
    for field in ["ingredients", "allergens"]:
        if isinstance(data.get(field), list):
            data[field] = ", ".join(data[field])

    # Create recipe using corrected keys
    recipe = Recipe(
        image=data["image"],
        name=data["name"],
        diet_type=data["diet_type"],
        allergens=data.get("allergens"),
        total_time=data["total_time"],
        ingredients=data.get("ingredients"),
        directionts=data["directions"],
        site=data["site"],
        calories=data.get("calories"),
        user_id=current_user.user_id,
        user_input=payload.user_text
    )

    create_recipe(recipe)

    return result
