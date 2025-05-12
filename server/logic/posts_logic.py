
from models import Users as UserModel
from models import Post as PostModel


def can_delete_post(
    user: UserModel, 
    post: PostModel
) -> bool:
    
    if user.role == 'ADMIN':
        return True

    if user.user_id == post.user_id:
        return True
    
    return False

