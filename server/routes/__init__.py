# routes/__init__.py
from fastapi import APIRouter

from .auth import router as auth_router
from .user import router as user_router
from .xp_badges import router as xp_router
from .activity_routes import router as activity_router
from .events import router as events_router
from .posts import router as posts_router
from .comments import router as comments_router
from .notifications import router as notifications_router
from .messages import router as messages_router
from .likes import router as likes_router
from .recipes import router as recipes_router

api_router = APIRouter()

api_router.include_router(auth_router)
api_router.include_router(user_router)
api_router.include_router(xp_router)
api_router.include_router(activity_router)
api_router.include_router(events_router)
api_router.include_router(posts_router)
api_router.include_router(comments_router)
api_router.include_router(notifications_router)
api_router.include_router(messages_router)
api_router.include_router(likes_router)
api_router.include_router(recipes_router)
