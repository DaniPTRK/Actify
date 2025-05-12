# crud.py
from datetime import datetime, date
from typing import Optional, Any, Mapping

from sqlalchemy import text
from sqlalchemy.engine import CursorResult
from models import *

from db import get_session


# ─────────────────────────────────────────────────────────────────────────────
# Internal helper
# ─────────────────────────────────────────────────────────────────────────────
def _insert(sql: str, params: dict[str, Any]) -> int:
    """
    Execute an INSERT and return the generated primary-key value.
    """
    with get_session() as session:
        result: CursorResult = session.execute(text(sql), params)
        # MySQL returns the autoincrement value here
        return result.lastrowid


# ─────────────────────────────────────────────────────────────────────────────
# Users
# ─────────────────────────────────────────────────────────────────────────────
def create_user(
    user_id: int,
    email: str,
    password_hash: str,
    name: str,
    role: str,
    bio: Optional[str] = None,
    preferences: Optional[str] = None,
) -> int:
    sql = """
        INSERT INTO Users
            (email, password_hash, name, role, bio,
             create_account_date, preferences)
        VALUES
            (:email, :password_hash, :name, :role, :bio,
             :create_account_date, :preferences)
    """
    params = dict(
        email=email,
        password_hash=password_hash,
        name=name,
        role=role,
        bio=bio,
        create_account_date=date.today(),   # MySQL DATE column
        preferences=preferences,
    )
    return _insert(sql, params)


# ─────────────────────────────────────────────────────────────────────────────
# UserProfiles
# ─────────────────────────────────────────────────────────────────────────────
def create_user_profile(
    profile_id: int, 
    user_id: int,
    goals: Optional[str] = None,
    dietary_restr: Optional[str] = None,
    avatar_url: Optional[str] = None,
) -> int:
    sql = """
        INSERT INTO UserProfiles
            (user_id, goals, dietary_restr, avatar_url)
        VALUES
            (:user_id, :goals, :dietary_restr, :avatar_url)
    """
    return _insert(sql, locals())


# ─────────────────────────────────────────────────────────────────────────────
# Postari (Posts)
# ─────────────────────────────────────────────────────────────────────────────
def create_post(
    post_id: int,
    user_id: int,
    content: str,
    media_url: Optional[str] = None,
) -> int:
    sql = """
        INSERT INTO Postari
            (user_id, content, media_url, timestamp)
        VALUES
            (:user_id, :content, :media_url, :timestamp)
    """
    params = dict(
        user_id=user_id,
        content=content,
        media_url=media_url,
        timestamp=datetime.utcnow(),
    )
    return _insert(sql, params)


# ─────────────────────────────────────────────────────────────────────────────
# Comments
# ─────────────────────────────────────────────────────────────────────────────
def create_comment(
    comment_id: int,
    post_id: int,
    user_id: int,
    content: str,
) -> int:
    sql = """
        INSERT INTO Comments
            (post_id, user_id, content, timestamp)
        VALUES
            (:post_id, :user_id, :content, :timestamp)
    """
    params = dict(
        post_id=post_id,
        user_id=user_id,
        content=content,
        timestamp=datetime.utcnow(),
    )
    return _insert(sql, params)


# ─────────────────────────────────────────────────────────────────────────────
# Likes
# ─────────────────────────────────────────────────────────────────────────────
def create_like(
    like_id: int,
    user_id: int,
    post_id: int,
) -> int:
    sql = """
        INSERT INTO Likes (user_id, post_id)
        VALUES (:user_id, :post_id)
    """
    return _insert(sql, locals())


# ─────────────────────────────────────────────────────────────────────────────
# Mesaje (Messages)
# ─────────────────────────────────────────────────────────────────────────────
def create_message(
    mesaj_id: int,
    id_sender: int,
    id_receiver: int,
    content: str,
) -> int:
    sql = """
        INSERT INTO Mesaje (id_sender, id_receiver, content)
        VALUES (:id_sender, :id_receiver, :content)
    """
    return _insert(sql, locals())


# ─────────────────────────────────────────────────────────────────────────────
# XP_Badges
# ─────────────────────────────────────────────────────────────────────────────
def create_xp_badge(
    record_id: int,
    user_id: int,
    xp_points: int,
    badge_name: str,
) -> int:
    sql = """
        INSERT INTO XP_Badges
            (user_id, xp_points, badge_name, earned_at)
        VALUES
            (:user_id, :xp_points, :badge_name, :earned_at)
    """
    params = dict(
        user_id=user_id,
        xp_points=xp_points,
        badge_name=badge_name,
        earned_at=datetime.utcnow(),
    )
    return _insert(sql, params)


# ─────────────────────────────────────────────────────────────────────────────
# EventParticipants
# ─────────────────────────────────────────────────────────────────────────────
def create_event_participant(
    participant_id: int,
    user_id: int,
    event_id: int,
    status: str,
) -> int:
    sql = """
        INSERT INTO EventParticipants (user_id, event_id, status)
        VALUES (:user_id, :event_id, :status)
    """
    return _insert(sql, locals())


# ─────────────────────────────────────────────────────────────────────────────
# Notifications
# ─────────────────────────────────────────────────────────────────────────────
def create_notification(
    notification_id: int, 
    user_id: int,
    notif_type: str,
    message: str,
    is_read: bool = False,
) -> int:
    sql = """
        INSERT INTO Notifications
            (user_id, type, message, is_read, timestamp)
        VALUES
            (:user_id, :notif_type, :message, :is_read, :timestamp)
    """
    params = dict(
        user_id=user_id,
        notif_type=notif_type,
        message=message,
        is_read=is_read,
        timestamp=datetime.utcnow(),
    )
    return _insert(sql, params)


# ─────────────────────────────────────────────────────────────────────────────
# Retete (Recipes)
# ─────────────────────────────────────────────────────────────────────────────
def create_recipe(
    id_reteta: int,
    title: str,
    description: str,
    ingredients: str,
    calories: int,
    servings: int,
) -> int:
    sql = """
        INSERT INTO Retete
            (title, description, ingredients, calories, servings)
        VALUES
            (:title, :description, :ingredients, :calories, :servings)
    """
    return _insert(sql, locals())


# ─────────────────────────────────────────────────────────────────────────────
# Events
# ─────────────────────────────────────────────────────────────────────────────
def create_event(
    event_id: int,
    title: str,
    description: str,
    location: str,
    date_time: datetime,
    max_participants: int,
) -> int:
    sql = """
        INSERT INTO Events
            (title, description, location, date_time, max_participants)
        VALUES
            (:title, :description, :location, :date_time, :max_participants)
    """
    return _insert(sql, locals())


# ─────────────────────────────────────────────────────────────────────────────
# Routes
# ─────────────────────────────────────────────────────────────────────────────
def create_route(
    route_id: int,
    user_id: int,
    activity_type: str,
    name: str,
    distance_km: float,
    estimated_time_min: int,
) -> int:
    sql = """
        INSERT INTO Routes
            (user_id, activity_type, name, distance_km, estimated_time_min)
        VALUES
            (:user_id, :activity_type, :name, :distance_km, :estimated_time_min)
    """
    return _insert(sql, locals())




        
def get_row_by_id(
    table: str,
    id_value: int,
    id_column: str = "id",
) -> Mapping[str, Any] | None:
    with get_session() as session:
        stmt = text(f"SELECT * FROM `{table}` WHERE `{id_column}` = {id_value}")
        print(stmt)
        rows = session.execute(stmt).all()

        for row in rows:
            print(row)
      
      
      
def get_all_rows(
    table: str,
) -> Mapping[str, Any] | None:
    with get_session() as session:
        stmt = text(f"SELECT * FROM `{table}`")
        print(stmt)
        rows = session.execute(stmt).all()

    for row in rows:
        print(row)

    

        
#get_row_by_id('Users', 1, 'user_id')


"""
user = create_user(
email="ana@example.com",
password_hash="pbkdf2:sha256:...",
name="Ana",
role="member",
bio="Trail runner & foodie",
)"""

#print(user.name)


#get_row_by_id('Users', 1, 'user_id')
get_all_rows('Users')