from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from dotenv import load_dotenv
load_dotenv(".env")

from routes import api_router

BACKEND_PORT = 8000

app = FastAPI(
    title="Actify API",
    version="0.1.0",
    description="Minimal FastAPI backend for the mobile client",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

print("trece pe aici12345")
app.include_router(api_router, prefix="/api")

@app.get("/")
async def root() -> dict[str, str]:
    return {"message": "Backend is up"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=BACKEND_PORT,
        reload=True,
        workers=1,
    )
