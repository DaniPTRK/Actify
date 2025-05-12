from typing import List
from fastapi import APIRouter, HTTPException, status, Depends
from sqlmodel import SQLModel

from services.activity_routes import (
    create_route,
    list_routes,
    get_route,
    update_route,
    delete_route,
)
from models import ActivityRoute as RouteModel
from models import Users as UserModel
from dependencies.token_verification import verify_jwt

router = APIRouter(
    prefix="/activity-routes", 
    tags=["Activity Routes"],
    dependencies=[Depends(verify_jwt)],
)


class RouteCreate(SQLModel):
    user_id: int
    activity_type: str
    name: str
    distance_km: float
    estimated_time_min: int


class RouteUpdate(SQLModel):
    activity_type: str | None = None
    name: str | None = None
    distance_km: float | None = None
    estimated_time_min: int | None = None


@router.get("", response_model=List[RouteModel])
async def read_routes(
    current_user: UserModel = Depends(verify_jwt)
):
    return list_routes()


@router.post("", response_model=RouteModel, status_code=status.HTTP_201_CREATED)
async def create(r: RouteCreate,
    current_user: UserModel = Depends(verify_jwt)
):
    return create_route(RouteModel(**r.dict()))


@router.get("/{route_id}", response_model=RouteModel)
async def read_route(
    route_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    r = get_route(route_id)
    if not r:
        raise HTTPException(status_code=404, detail="Route not found")
    return r


@router.put("/{route_id}", response_model=RouteModel)
async def replace_route(
    route_id: int, r: RouteUpdate,
    current_user: UserModel = Depends(verify_jwt)
):
    updated = update_route(route_id, r.dict(exclude_unset=True))
    if not updated:
        raise HTTPException(status_code=404, detail="Route not found")
    return updated


@router.delete("/{route_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_route(
    route_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    if not delete_route(route_id):
        raise HTTPException(status_code=404, detail="Route not found")
