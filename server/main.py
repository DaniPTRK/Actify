from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from routes import api_router
from db import init_db


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

init_db()


app.include_router(router, prefix="/api")

@app.get("/")
async def root() -> dict[str, str]:
    return {"message": "Backend is up"}
