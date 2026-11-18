# Backend changes needed for Part 3 (FastAPI on Render)

Your backend source wasn't included in what I received, so this is a **template**:
rename models, dependencies and helpers to match your `backend/app/` code.
The Android app calls two new endpoints.

## 1. `POST /auth/google` (single sign-on)

Request: `{ "id_token": "<Google ID token>" }` → response: the **same shape as `/auth/login`**
(`access_token`, `refresh_token`, `token_type`, `user`).

```python
# pip install google-auth requests     (add to requirements.txt)
from google.oauth2 import id_token as google_id_token
from google.auth.transport import requests as g_requests

class GoogleLoginRequest(BaseModel):
    id_token: str

@router.post("/google", response_model=TokenResponse)
def google_login(body: GoogleLoginRequest, db: Session = Depends(get_db)):
    try:
        info = google_id_token.verify_oauth2_token(
            body.id_token, g_requests.Request(), os.environ["GOOGLE_WEB_CLIENT_ID"]
        )
    except ValueError:
        raise HTTPException(status_code=401, detail="Invalid Google token")

    email = info["email"]
    user = db.query(User).filter(User.email == email).first()
    if user is None:
        default_area = db.query(Area).first()          # new SSO users start in a default area
        user = User(full_name=info.get("name", email), email=email,
                    mobile="", area_id=default_area.area_id if default_area else None,
                    password_hash=hash_password(secrets.token_urlsafe(32)), is_verified=True)
        db.add(user); db.commit(); db.refresh(user)
    return build_token_response(user)                   # reuse what /auth/login returns
```

Set `GOOGLE_WEB_CLIENT_ID` as an environment variable on Render. It must be the **Web** OAuth client ID
(the same value you put in `local.properties` as `GOOGLE_WEB_CLIENT_ID`).

## 2. `POST /users/me/device-token` (push)

Request: `{ "token": "<FCM token>" }` → any 2xx.

```python
class DeviceToken(Base):
    __tablename__ = "device_tokens"
    token = Column(String, primary_key=True)
    user_id = Column(String, ForeignKey("users.user_id"), index=True)

@router.post("/me/device-token", status_code=204)
def register_device_token(body: DeviceTokenIn, user=Depends(current_user), db=Depends(get_db)):
    db.merge(DeviceToken(token=body.token, user_id=user.user_id)); db.commit()
```

## 3. Sending a push (e.g. when a safety-alert post is created)

```python
# pip install firebase-admin ; service-account JSON in the FIREBASE_CREDENTIALS env var
import firebase_admin, json, os
from firebase_admin import credentials, messaging
if not firebase_admin._apps:
    firebase_admin.initialize_app(credentials.Certificate(json.loads(os.environ["FIREBASE_CREDENTIALS"])))

def push_to_area(db, area_id, title, body, post_id, type_="SAFETY_ALERT"):
    tokens = [t.token for t in db.query(DeviceToken).join(User).filter(User.area_id == area_id)]
    if tokens:
        messaging.send_each_for_multicast(messaging.MulticastMessage(
            tokens=tokens,
            data={"type": type_, "title": title, "body": body, "post_id": post_id},  # DATA message
        ))
```

Send **data-only** messages (no `notification` block) so the app's service always runs and
stores the alert in the in-app Notifications screen.

## 4. `/sync/push`

The app now sends `client_post_id` inside post payloads and expects `server_id` in each result.
Best behaviour: **use `client_post_id` as the post's primary key** so the local copy and the server
copy are the same row. (If you return a different `server_id`, the app deletes its local draft and
downloads the server's copy.)
