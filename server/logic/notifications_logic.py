
from models import User as UserModel
from models import Notification as NotificationModel


def can_delete_notification(
    user: UserModel, 
    notification: NotificationModel
) -> bool:
    
    if user.role == 'ADMIN':
        return True

    if user.user_id == notification.user_id:
        return True
    
    return False
    