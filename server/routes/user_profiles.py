from typing import List
from fastapi import APIRouter, HTTPException, status, Depends
from sqlmodel import SQLModel

from services.user_profiles import *
from services.user import get_user_by_id
from models import UserProfiles as UserProfilesModel
from dependencies.token_verification import verify_jwt
from logic.users_logic import *

router = APIRouter(
    prefix="/user_profiles",
    tags=["UserProfiles"],
    dependencies=[Depends(verify_jwt)]
)


class UserProfilesCreate(SQLModel):
    user_id: int
    goals: str | None = None
    dietary_restr: str | None = None
    avatar_url: str | None = None



@router.post(
    "/",
    response_model=UserProfilesCreate,
    status_code=status.HTTP_201_CREATED,
)
async def create(
    profile: UserProfilesCreate,
    current_user: UserModel = Depends(verify_jwt)
):
    return create_user_profile(UserProfilesModel(**profile.dict()))


''' get the profile by user_id'''
@router.get("/{user_id}", response_model=UserProfilesCreate)
async def read_one_by_user_id(
    user_id: int,
    current_user: UserModel = Depends(verify_jwt)
):
    profile = get_user_profile_by_user_id(user_id)

    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")

    return profile


@router.put("/{user_id}", response_model=UserProfilesCreate)
async def replace(
    user_id: int,
    new_profile: UserProfilesCreate,
    current_user: UserModel = Depends(verify_jwt)
):
    user = get_user_by_id(user_id)
    profile = get_user_profile_by_user_id(user_id)
    
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    
    if not can_modify_user(user, current_user):
        raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="You dont have the permission to change this user's data",
            )

    updated = update_user_profile(profile, new_profile.dict())

    if not updated:
        raise HTTPException(status_code=404, detail="Profile not found")

    return updated
