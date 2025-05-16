from typing import List
from datetime import datetime
from fastapi import APIRouter, HTTPException, status, Depends
from sqlmodel import SQLModel, Field
from typing import Optional, List
import sys
import os

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "../..")))

from services.recipes import (
    create_recipe,
    list_recipes,
    get_recipe,
    update_recipe,
    delete_recipe,
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
    id: Optional[int] = Field(default=None, primary_key=True)
    image: str
    name: str
    diet_type: str
    allergens: Optional[str]
    total_time: int
    ingredients: Optional[str]
    directionts: str
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
        raise HTTPException(status_code=406, detail=result["Did't find any content that conforms to the criteria given by the user agent."])
    
    
    data = rec.dict(exclude_unset=True)

    if data.get("created_at") is None:
        data["created_at"] = datetime.utcnow()

    create_recipe(RecipeModel(**data))
    
    return result
