from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from routes import api_router
from db import engine
from CRUD import create_user, create_post


FRONTEND_PORT = 10001

app = FastAPI(
    title="Actify API",
    version="0.1.0",
    description="Minimal FastAPI backend for the mobile client",
)

origins = [
    "http://localhost",
    "http://localhost:{FRONTEND_PORT}}",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

print(engine)


app.include_router(api_router, prefix="/api")

@app.get("/")
async def root() -> dict[str, str]:
    return {"message": "Backend is up"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        workers=1,
    )


from services.user import create_user as fff
from models import *

user = User(email = 'asdhgfajsd@gmail.com', password_hash = 'asjdffgasjhdjasd')

#print(user.dict())
