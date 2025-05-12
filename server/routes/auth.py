from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel

from services.auth import *

from dependencies.token_verification import create_access_token

from routes.user import UserCreate
from datetime import timedelta

router = APIRouter(tags=["Auth"])

@router.get("/ping")
async def ping() -> dict[str, str]:
    return {"message": "pong"}



@router.post("/auth/register", status_code=status.HTTP_201_CREATED)
async def register(creds: UserCreate) -> dict[str, str]:
    register_user(creds)
    return {"message": "registered"}


class Credentials(BaseModel):
    email: str
    password_hash: str


@router.post("/auth/login")
async def login(creds: Credentials) -> dict[str, str]:
    user = authenticate_user(creds.email, creds.password_hash)
    
    token = create_access_token(
        subject=user.user_id,
        expires_delta=timedelta(hours=12),
        extra_claims={"role": user.role}
    )

    return {"message": "logged in", "token": token}
