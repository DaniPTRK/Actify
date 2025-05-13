from models import Users as UserModel


def can_modify_user(
    user: UserModel,
    logged_user: UserModel
) -> bool:
    
    if logged_user.role == 'ADMIN':
        return True

    if user.user_id == logged_user.user_id:
        return True
    
    return False


