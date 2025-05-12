from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel

from services.auth import *

router = APIRouter(tags=["Auth"])

@router.get("/ping")
async def ping() -> dict[str, str]:
    return {"message": "pong"}


class Credentials(BaseModel):
    username: str
    password: str



@router.post("/auth/register", status_code=status.HTTP_201_CREATED)
async def register(creds: Credentials) -> dict[str, str]:
    register_user(creds.username, creds.password)
    return {"message": "registered"}



@router.post("/auth/login")
async def login(creds: Credentials) -> dict[str, str]:
    authenticate_user(creds.username, creds.password)
    return {"message": "logged in"}
