from contextlib import contextmanager
import os
from sqlmodel import SQLModel, create_engine, Session


MYSQL_USER = os.getenv("MYSQL_USER", "actify")
MYSQL_PASS = os.getenv("MYSQL_PASS", "secret")
MYSQL_HOST = os.getenv("MYSQL_HOST", "localhost")
MYSQL_PORT = os.getenv("MYSQL_PORT", "3306")
MYSQL_DB   = os.getenv("MYSQL_DB",   "actify")

DATABASE_URL = (
    "mysql+mysqlconnector://"
    f"{MYSQL_USER}:{MYSQL_PASS}@{MYSQL_HOST}:{MYSQL_PORT}/{MYSQL_DB}"
)

engine = create_engine(DATABASE_URL, echo=False, pool_pre_ping=True)


def init_db() -> None:
    """Create tables (no-op if they already exist). Call once at startup."""
    SQLModel.metadata.create_all(engine)


@contextmanager
def get_session() -> Session:
    """Context-manager that yields a SQLModel Session and commits on exit."""
    with Session(engine) as session:
        try:
            yield session
            session.commit()
        except Exception:
            session.rollback()
            raise
