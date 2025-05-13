from fastapi import APIRouter, HTTPException, status, Depends, Request
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import jwt
import os
from services.user import get_user_by_id
from datetime import datetime, timedelta
from models import Users

# JWT configuration
SECRET_KEY = os.getenv('token_secret_key')
ALGORITHM = "HS256"

# HTTP Bearer security scheme
security = HTTPBearer()

async def verify_jwt(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    ) -> Users:
    """
    1) Pulls Bearer token out of Authorization header.
    2) Verifies signature + standard claims (exp, nbf, etc.).
    3) Extracts the 'sub' claim and loads the user from MySQL.
    4) Raises 401/404/403 as appropriate, or returns the UserModel.
    """
    token = credentials.credentials
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except jwt.PyJWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
        )

    user_id = payload.get("sub")
    if user_id is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token missing subject",
        )

    user = get_user_by_id(int(user_id))
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Users not found")

    return user




def decode_and_get_user(token: str):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except jwt.PyJWTError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                            detail="Invalid or expired token")
    user_id = payload.get("sub")
    if user_id is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                            detail="Token missing subject")
    user = get_user(int(user_id))
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND,
                            detail="Users not found")

    return user





def create_access_token(
    *,
    subject: int | str,
    expires_delta: timedelta | None = None,
    extra_claims: dict | None = None,  # optional: add role, email, etc.
) -> str:
    """
    Return a signed JWT (HS256) encoding:
      * sub  - user_id (as str)
      * iat  - issued-at (UTC)
      * nbf  - not-before (UTC, same as iat)
      * exp  - expiry (UTC, default 1 h)
      * any extra key/value pairs you pass via *extra_claims*
    """
    if not SECRET_KEY:
        raise RuntimeError("ENV var 'token_secret_key' is unset")

    now = datetime.utcnow()
    if expires_delta is None:
        expires_delta = timedelta(hours=1)      # default lifetime
    exp = now + expires_delta

    payload = {
        "sub": str(subject),
        "iat": now,
        "nbf": now,
        "exp": exp,
    }
    if extra_claims:
        payload.update(extra_claims)

    token: str = jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)
    return token


