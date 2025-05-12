from datetime import datetime
from typing import Optional

from sqlmodel import SQLModel, Field


class XPBadge(SQLModel, table=True):
    __tablename__ = "XP_Badges"
    record_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    xp_points: int
    badge_name: str
    earned_at: datetime = Field(default_factory=datetime.utcnow)


class ActivityRoute(SQLModel, table=True):
    __tablename__ = "Routes"
    route_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    activity_type: str
    name: str
    distance_km: float
    estimated_time_min: int


class Event(SQLModel, table=True):
    __tablename__ = "Events"
    event_id: Optional[int] = Field(default=None, primary_key=True)
    title: str
    description: str
    location: str
    date_time: datetime
    max_participants: int


class Post(SQLModel, table=True):
    __tablename__ = "Postari"
    post_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    content: str
    media_url: Optional[str] = None
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class Comment(SQLModel, table=True):
    __tablename__ = "Comments"
    comment_id: Optional[int] = Field(default=None, primary_key=True)
    post_id: int = Field(index=True)
    user_id: int = Field(index=True)
    content: str
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    
class Users(SQLModel, table=True):
    __tablename__ = "Users"
    user_id: Optional[int] = Field(default=None, primary_key=True)
    email: str = Field(index=True, unique=True)
    password_hash: str
    name: Optional[str] = None
    role: Optional[str] = None
    bio: Optional[str] = None
    preferences: Optional[str] = None
    
class Notification(SQLModel, table=True):
    __tablename__ = "Notifications"
    notification_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    content: str
    type: Optional[str] = None
    created_at: datetime = Field(default_factory=datetime.utcnow)
    read: bool = Field(default=False)


class Message(SQLModel, table=True):
    __tablename__ = "Mesaje"
    mesaj_id: Optional[int] = Field(default=None, primary_key=True)
    sender_id: int = Field(index=True)
    receiver_id: int = Field(index=True)
    content: str
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    read: bool = Field(default=False)


class Like(SQLModel, table=True):
    __tablename__ = "Likes"
    like_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    post_id: Optional[int] = Field(default=None, index=True)
    comment_id: Optional[int] = Field(default=None, index=True)
    created_at: datetime = Field(default_factory=datetime.utcnow)


class Recipe(SQLModel, table=True):
    __tablename__ = "Retete"
    recipe_id: Optional[int] = Field(default=None, primary_key=True)
    author_id: int = Field(index=True)
    title: str
    description: str
    ingredients: str
    steps: str
    created_at: datetime = Field(default_factory=datetime.utcnow)
