from contextlib import contextmanager
import os
from sqlmodel import create_engine, Session

MYSQL_USER = os.getenv("MYSQL_USER")
MYSQL_PASS = os.getenv("MYSQL_PASS")
MYSQL_HOST = os.getenv("MYSQL_HOST")
MYSQL_PORT = os.getenv("MYSQL_PORT")
MYSQL_DB = os.getenv("MYSQL_DB")



DATABASE_URL = (
    "mysql+mysqlconnector://"
    f"{MYSQL_USER}:{MYSQL_PASS}@{MYSQL_HOST}:{MYSQL_PORT}/{MYSQL_DB}"
)

engine = create_engine(DATABASE_URL, echo=False, pool_pre_ping=True)
from models import Recipe
Recipe.metadata.create_all(engine, tables=[Recipe.__table__])

@contextmanager
def get_session() -> Session:
    """Context-manager that yields a SQLModel Session and commits on exit."""
    with Session(engine, expire_on_commit=False) as session:
        try:
            yield session
            session.commit()
        except Exception:
            session.rollback()
            raise
        
        
def get_all_tables_and_describe() -> None:
    from sqlalchemy import inspect

    inspector = inspect(engine)
    table_names = inspector.get_table_names()

    print(f"\nFound {len(table_names)} table(s):\n")

    for table in table_names:
        print(f"Table: {table}")
        columns = inspector.get_columns(table)
        for col in columns:
            name = col["name"]
            dtype = col["type"]
            nullable = col["nullable"]
            default = col.get("default", None)
            print(f"  - {name} ({dtype}), nullable: {nullable}, default: {default}")
        print("-" * 40)
    
    
def get_and_print_table_entries(table_name: str) -> None:
    from sqlalchemy import inspect, text

    inspector = inspect(engine)

    if table_name not in inspector.get_table_names():
        print(f"❌ Table '{table_name}' not found in database.")
        return

    print(f"\nTable: {table_name}")
    columns = inspector.get_columns(table_name)
    for col in columns:
        name = col["name"]
        dtype = col["type"]
        nullable = col["nullable"]
        default = col.get("default", None)
        print(f"  - {name} ({dtype}), nullable: {nullable}, default: {default}")

    print("\n📦 Entries:")
    with Session(engine) as session:
        result = session.execute(text(f"SELECT * FROM `{table_name}`")).mappings().all()

        if not result:
            print("  (no data)")
        else:
            for row in result:
                print(f"  {dict(row)}")
    
#get_and_print_table_entries('Retete')        
#get_all_tables_and_describe()

