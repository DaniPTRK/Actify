from typing import Dict, List
from fastapi import WebSocket

class ConnectionManager:
    def __init__(self):
        # map user_id → list of WebSocket connections
        self.active_connections: Dict[int, List[WebSocket]] = {}

    async def connect(self, user_id: int, websocket: WebSocket):
        await websocket.accept()
        conns = self.active_connections.setdefault(user_id, [])
        conns.append(websocket)

    def disconnect(self, user_id: int, websocket: WebSocket):
        conns = self.active_connections.get(user_id, [])
        if websocket in conns:
            conns.remove(websocket)
            if not conns:
                del self.active_connections[user_id]

    async def send_personal_message(self, message: dict, user_id: int):
        """Send JSON message to all sockets for this user."""
        for websocket in self.active_connections.get(user_id, []):
            await websocket.send_json(message)


manager = ConnectionManager()
