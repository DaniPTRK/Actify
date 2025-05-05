"""
Basic routes consumed by the frontend.
Feel free to split these into multiple files as the app grows.
"""

from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel
from typing import Dict

router = APIRouter(tags=["Basic"])

# ------------------------------------------------------------------
# Health‑check ------------------------------------------------------
# ------------------------------------------------------------------
@router.get("/ping")
async def ping() -> dict[str, str]:
    return {"message": "pong"}


# ------------------------------------------------------------------
# In‑memory demo “database” ----------------------------------------
# (Swap out for SQLAlchemy, Tortoise‑ORM, or another real DB later)
# ------------------------------------------------------------------
_fake_users: Dict[str, str] = {}  # username -> hashed_password


# ------------------------------------------------------------------
# Auth models -------------------------------------------------------
# ------------------------------------------------------------------
class Credentials(BaseModel):
    username: str
    password: str


# ------------------------------------------------------------------
# Registration ------------------------------------------------------
# ------------------------------------------------------------------
@router.post("/auth/register", status_code=status.HTTP_201_CREATED)
async def register(creds: Credentials) -> dict[str, str]:
    if creds.username in _fake_users:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="User already exists",
        )

    # In production: store a **hashed** password, not plaintext!
    _fake_users[creds.username] = creds.password
    return {"message": "registered"}


# ------------------------------------------------------------------
# Login -------------------------------------------------------------
# ------------------------------------------------------------------
@router.post("/auth/login")
async def login(creds: Credentials) -> dict[str, str]:
    if _fake_users.get(creds.username) != creds.password:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid credentials",
        )


    return {"message": "logged in"}


# ------------------------------------------------------------------
# Profile endpoint (example) ---------------------------------------
# ------------------------------------------------------------------
@router.get("/users/{username}")
async def profile(username: str) -> dict[str, str]:
    if username not in _fake_users:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Not found")
    return {"username": username}
