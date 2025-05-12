from typing import List
from fastapi import APIRouter, HTTPException, status, Depends
from sqlmodel import SQLModel

from services.user import (
    create_user,
    list_users,
    get_user,
    update_user,
    delete_user,
)
from models import User as UserModel
from dependencies.token_verification import verify_jwt

router = APIRouter(
    prefix="/users",
    tags=["User"],
    dependencies=[Depends(verify_jwt)]
)


class UserCreate(SQLModel):
    username: str
    password_hash: str
    name: str | None = None
    role: str | None = None
    bio: str | None = None
    preferences: str | None = None


class UserRead(SQLModel):
    user_id: int
    username: str
    name: str | None
    role: str | None
    bio: str | None
    preferences: str | None

    class Config:
        orm_mode = True


@router.post(
    "",
    response_model=UserRead,
    status_code=status.HTTP_201_CREATED,
)
async def create(
    user: UserCreate,
    current_user: UserModel = Depends(verify_jwt)
):
    return create_user(UserModel(**user.dict()))


@router.get("", response_model=List[UserRead])
async def read_all(
    current_user: UserModel = Depends(verify_jwt)
):
    return list_users()


@router.get("/{user_id}", response_model=UserRead)
async def read_one(
    user_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    user = get_user(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.put("/{user_id}", response_model=UserRead)
async def replace(
    user_id: int,
    user: UserCreate,
    current_user: UserModel = Depends(verify_jwt)
):
    updated = update_user(user_id, user.dict())
    if not updated:
        raise HTTPException(status_code=404, detail="User not found")
    return updated


@router.delete("/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove(
    user_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    if not delete_user(user_id):
        raise HTTPException(status_code=404, detail="User not found")
