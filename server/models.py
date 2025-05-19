from datetime import datetime
from typing import Optional, List

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
    create_account_date: datetime = Field(default_factory=datetime.utcnow)

class UserProfiles(SQLModel, table=True):
    __tablename__ = "UserProfiles"
    profile_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    goals: Optional[str]
    dietary_restr: Optional[str]
    avatar_url: Optional[str]
    
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
    post_id: int
    created_at: datetime = Field(default_factory=datetime.utcnow)


class Recipe(SQLModel, table=True):
    __tablename__ = "Retete"    
    recipe_id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    user_input: str
    image: str
    name: str
    diet_type: str
    allergens: Optional[str]
    total_time: int
    ingredients: Optional[str]
    site: str
    calories: float | None = None
    created_at: datetime = Field(default_factory=datetime.utcnow)
