from typing import List
from fastapi import APIRouter, HTTPException, status, Depends
from sqlmodel import SQLModel

from services.user import *
from models import Users as UserModel
from dependencies.token_verification import verify_jwt
from logic.users_logic import *

router = APIRouter(
    prefix="/users",
    tags=["User"],
    dependencies=[Depends(verify_jwt)]
)


class UserCreate(SQLModel):
    email: str
    password_hash: str
    name: str | None = None
    role: str | None = None
    bio: str | None = None
    preferences: str | None = None


class UserRead(SQLModel):
    user_id: int
    email: str
    name: str | None
    role: str | None
    bio: str | None
    preferences: str | None

    class Config:
        orm_mode = True


"""
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
"""

@router.get("/", response_model=List[UserRead])
async def read_all(
    current_user: UserModel = Depends(verify_jwt)
):
    return list_users()


''' get user by user_id'''
@router.get("/{user_id}/", response_model=UserRead)
async def read_one_by_user_id(
    user_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    user = get_user_by_id(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


''' get user by email'''
@router.get("/email/{email}/", response_model=UserRead)
async def read_one_by_email(
    email: str,
    current_user: UserModel = Depends(verify_jwt)
):
    user = get_user_by_email(email)

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    return user


''' get users by name'''
@router.get("/name/{name}/", response_model=UserRead)
async def read_all_by_name(
    name: str,
    current_user: UserModel = Depends(verify_jwt)
):
    users = get_users_by_name(name)

    if not users:
        raise HTTPException(status_code=404, detail="User not found")

    return users


@router.put("/{user_id}/", response_model=UserRead)
async def replace(
    user_id: int,
    new_user: UserCreate,
    current_user: UserModel = Depends(verify_jwt)
):
    user = get_user_by_id(user_id)
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    if not can_modify_user(user, current_user):
        raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="You dont have the permission to change this user's data",
            )

    updated = update_user(user, new_user.dict())

    if not updated:
        raise HTTPException(status_code=404, detail="User not found")

    return updated


@router.delete("/{user_id}/", status_code=status.HTTP_204_NO_CONTENT)
async def remove(
    user_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    
    user = get_user_by_id(user_id)
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    

    if not can_modify_user(user, current_user):
        raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="You dont have the permission to delete this user",
            )

    if not delete_user(user):
        raise HTTPException(status_code=404, detail="User not found")
