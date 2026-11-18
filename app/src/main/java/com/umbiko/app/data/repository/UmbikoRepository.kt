package com.umbiko.app.data.repository

import android.util.Log
import com.umbiko.app.data.TokenManager
import com.umbiko.app.data.local.AppDatabase
import com.umbiko.app.data.local.CommentEntity
import com.umbiko.app.data.local.NotificationLogEntity
import com.umbiko.app.data.local.OutboxEntity
import com.umbiko.app.data.local.PostEntity
import com.umbiko.app.data.local.SettingsEntity
import com.umbiko.app.data.local.UserEntity
import com.umbiko.app.data.remote.ApiService
import com.umbiko.app.data.remote.AreaResponseDto
import com.umbiko.app.data.remote.CategoryResponseDto
import com.umbiko.app.data.remote.DeviceTokenDto
import com.umbiko.app.data.remote.EventDetailInDto
import com.umbiko.app.data.remote.GoogleLoginRequestDto
import com.umbiko.app.data.remote.LoginRequestDto
import com.umbiko.app.data.remote.NetworkModule
import com.umbiko.app.data.remote.OutboxOperationDto
import com.umbiko.app.data.remote.PostCreateDto
import com.umbiko.app.data.remote.PostResponseDto
import com.umbiko.app.data.remote.RegisterRequestDto
import com.umbiko.app.data.remote.SettingsUpdateDto
import com.umbiko.app.data.remote.SyncPushRequestDto
import com.umbiko.app.data.remote.TokenResponseDto
import com.umbiko.app.data.remote.safeApiCall
import com.umbiko.app.domain.ApiResult
import com.umbiko.app.domain.NewPostDraft
import com.umbiko.app.domain.PostValidation
import com.umbiko.app.push.PushTokenProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.Instant
import java.util.UUID

private const val TAG = "UmbikoRepository"

class UmbikoRepository(
    private val api: ApiService,
    private val db: AppDatabase,
    private val tokenManager: TokenManager,
    private val onOutboxChanged: () -> Unit = {},
    private val onNotification: (title: String, body: String) -> Unit = { _, _ -> },
) {
    // Set to false to talk to the real API again.
    private val localOnly = true

    private val postDao = db.postDao()
    private val commentDao = db.commentDao()
    private val outboxDao = db.outboxDao()
    private val notificationDao = db.notificationDao()
    private val settingsDao = db.settingsDao()
    private val userDao = db.userDao()
    private val json: Json = NetworkModule.json

    private fun hash(password: String, salt: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest((salt + password).toByteArray())
            .joinToString("") { "%02x".format(it) }

    init {
        runBlocking(Dispatchers.IO) {
            try {
                // Seed once only, so RSVP counts are not reset on every app launch.
                if (postDao.getPost("demo-notice-1") == null) {
                    val now = Instant.now().toString()
                    val demoPosts = listOf(
                        PostEntity(
                            postId = "demo-notice-1",
                            authorId = "system",
                            authorName = "City Council",
                            areaId = "",
                            categoryId = "cat-water",
                            type = "NOTICE",
                            title = "Scheduled Water Supply Maintenance",
                            body = "Municipal maintenance will affect taps in our area this Friday from 9:00 AM to 2:00 PM. Please store drinking water in advance.",
                            createdAt = now,
                            updatedAt = now,
                            syncState = "SYNCED",
                        ),
                        PostEntity(
                            postId = "demo-notice-2",
                            authorId = "system",
                            authorName = "Neighborhood Watch",
                            areaId = "",
                            categoryId = "cat-safety",
                            type = "NOTICE",
                            title = "Community Safety Meeting",
                            body = "All residents are invited to attend our monthly neighborhood safety meeting at the community hall this Thursday at 6:30 PM.",
                            createdAt = now,
                            updatedAt = now,
                            syncState = "SYNCED",
                        ),
                        PostEntity(
                            postId = "demo-event-1",
                            authorId = "system",
                            authorName = "Youth Club",
                            areaId = "",
                            categoryId = "cat-sports",
                            type = "EVENT",
                            title = "Youth Sports Day & Braai",
                            body = "Join us at the community sports grounds for soccer, netball, and a community braai afterwards. All families welcome!",
                            eventStartsAt = "2026-12-15T10:00:00Z",
                            eventEndsAt = "2026-12-15T16:00:00Z",
                            eventVenueName = "Community Sports Grounds",
                            createdAt = now,
                            updatedAt = now,
                            syncState = "SYNCED",
                            rsvpGoing = 14,
                            rsvpInterested = 8,
                        ),
                        PostEntity(
                            postId = "demo-event-2",
                            authorId = "system",
                            authorName = "Chamber of Commerce",
                            areaId = "",
                            categoryId = "cat-market",
                            type = "EVENT",
                            title = "Local Entrepreneur Saturday Market",
                            body = "Support local small businesses showcasing handmade crafts, fresh produce, and delicious food at the town hall.",
                            eventStartsAt = "2026-12-20T08:00:00Z",
                            eventEndsAt = "2026-12-20T14:00:00Z",
                            eventVenueName = "Town Hall Courtyard",
                            createdAt = now,
                            updatedAt = now,
                            syncState = "SYNCED",
                            rsvpGoing = 25,
                            rsvpInterested = 12,
                        ),
                    )
                    postDao.upsertAll(demoPosts)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to seed demo posts: ${e.message}")
            }
        }
    }

    val isLoggedIn: Boolean get() = tokenManager.isLoggedIn()
    val currentUserId: String? get() = tokenManager.userId
    val currentAreaId: String? get() = tokenManager.areaId

    // ---------------------------------------------------------------- auth

    suspend fun register(fullName: String, email: String, mobile: String, areaId: String, password: String): ApiResult<Unit> {
        if (localOnly) {
            val mail = email.trim().lowercase()
            if (userDao.findByEmail(mail) != null) {
                return ApiResult.Error("An account with this email already exists")
            }
            val salt = UUID.randomUUID().toString()
            val user = UserEntity(
                userId = UUID.randomUUID().toString(),
                fullName = fullName,
                email = mail,
                mobile = mobile,
                areaId = areaId,
                salt = salt,
                passwordHash = hash(password, salt),
            )
            userDao.insert(user)
            startLocalSession(user)
            return ApiResult.Success(Unit)
        }
        val result = safeApiCall {
            api.register(RegisterRequestDto(fullName, email, mobile, areaId, password))
        }
        if (result is ApiResult.Success) {
            persistSession(result.data)
            syncSessionExtras()
        }
        return result.map { }
    }

    suspend fun login(email: String, password: String): ApiResult<Unit> {
        if (localOnly) {
            val user = userDao.findByEmail(email.trim().lowercase())
            if (user == null || user.passwordHash != hash(password, user.salt)) {
                return ApiResult.Error("Incorrect email or password")
            }
            startLocalSession(user)
            return ApiResult.Success(Unit)
        }
        val result = safeApiCall { api.login(LoginRequestDto(email, password)) }
        if (result is ApiResult.Success) {
            persistSession(result.data)
            syncSessionExtras()
        }
        return result.map { }
    }

    suspend fun loginWithGoogle(idToken: String): ApiResult<Unit> {
        if (localOnly) {
            return ApiResult.Error("Google sign-in needs the server. Use email and password.")
        }
        val result = safeApiCall { api.googleLogin(GoogleLoginRequestDto(idToken)) }
        if (result is ApiResult.Success) {
            Log.i(TAG, "Google SSO login succeeded")
            persistSession(result.data)
            syncSessionExtras()
        }
        return result.map { }
    }

    private fun startLocalSession(u: UserEntity) {
        tokenManager.saveSession("local-token", "local-refresh", u.userId, u.fullName, u.email, u.areaId)
    }

    private fun persistSession(t: TokenResponseDto) {
        tokenManager.saveSession(t.accessToken, t.refreshToken, t.user.userId, t.user.fullName, t.user.email, t.user.areaId)
    }

    suspend fun syncSessionExtras() {
        if (!isLoggedIn || localOnly) return
        loadRemoteSettingsIntoCache()
        PushTokenProvider.currentToken()?.let { registerPushToken(it) }
    }

    suspend fun registerPushToken(token: String) {
        if (!isLoggedIn || localOnly) return
        val result = safeApiCall { api.registerDeviceToken(DeviceTokenDto(token)) }
        Log.i(TAG, "Device token registration: ${result::class.simpleName}")
    }

    suspend fun recordPushNotification(type: String, title: String, body: String, relatedPostId: String?) {
        logNotification(type, title, body, relatedPostId, notify = true)
    }

    suspend fun logout() {
        if (!localOnly) runCatching { drainOutbox() }
        tokenManager.clear()
        // In local-only mode the users table must survive logout, otherwise accounts are lost.
        if (!localOnly) withContext(Dispatchers.IO) { db.clearAllTables() }
        Log.i(TAG, "Logged out")
    }

    // ------------------------------------------------------ areas / categories

    suspend fun listAreas(query: String? = null): ApiResult<List<AreaResponseDto>> =
        if (localOnly) ApiResult.Success(emptyList()) // AuthViewModel falls back to DEFAULT_AREAS
        else safeApiCall { api.listAreas(query) }

    suspend fun listCategories(): ApiResult<List<CategoryResponseDto>> =
        if (localOnly) {
            ApiResult.Success(
                listOf(
                    CategoryResponseDto("cat-water", "water"),
                    CategoryResponseDto("cat-safety", "safety"),
                    CategoryResponseDto("cat-sports", "sports"),
                    CategoryResponseDto("cat-market", "market"),
                    CategoryResponseDto("cat-other", "other"),
                )
            )
        } else safeApiCall { api.listCategories() }

    // ------------------------------------------------------------- settings

    fun observeSettings(): Flow<SettingsEntity?> = settingsDao.observe()

    suspend fun updateSettings(update: SettingsUpdateDto): ApiResult<Unit> {
        val current = settingsDao.get() ?: SettingsEntity()
        val merged = current.copy(
            languageCode = update.languageCode ?: current.languageCode,
            radiusKm = update.radiusKm ?: current.radiusKm,
            pushOptIn = update.pushOptIn ?: current.pushOptIn,
            syncOnWifiOnly = update.syncOnWifiOnly ?: current.syncOnWifiOnly,
            biometricUnlock = update.biometricUnlock ?: current.biometricUnlock,
        )
        settingsDao.upsert(merged)
        mirrorSettings(merged)
        if (localOnly) return ApiResult.Success(Unit)

        val result = safeApiCall { api.updateSettings(update) }
        if (result is ApiResult.NetworkError) {
            queueSettingsSync(update)
        }
        return result.map { }
    }

    suspend fun loadRemoteSettingsIntoCache(): ApiResult<Unit> {
        if (localOnly) return ApiResult.Success(Unit)
        val result = safeApiCall { api.getSettings() }
        if (result is ApiResult.Success) {
            val remote = result.data
            val current = settingsDao.get() ?: SettingsEntity()
            val merged = current.copy(
                languageCode = remote.languageCode,
                radiusKm = remote.radiusKm,
                pushOptIn = remote.pushOptIn,
                syncOnWifiOnly = remote.syncOnWifiOnly,
                biometricUnlock = remote.biometricUnlock,
            )
            settingsDao.upsert(merged)
            mirrorSettings(merged)
        }
        return result.map { }
    }

    private fun mirrorSettings(s: SettingsEntity) {
        tokenManager.syncOnWifiOnly = s.syncOnWifiOnly
        tokenManager.biometricUnlock = s.biometricUnlock
    }

    private suspend fun queueSettingsSync(update: SettingsUpdateDto) {
        val payload = json.encodeToString(SettingsUpdateDto.serializer(), update)
        outboxDao.insert(
            OutboxEntity(
                queueId = UUID.randomUUID().toString(),
                entityName = "settings",
                operation = "UPDATE",
                payloadJson = payload,
                createdAt = System.currentTimeMillis(),
            )
        )
        onOutboxChanged()
    }

    // ---------------------------------------------------------------- posts

    fun observePosts(type: String? = null): Flow<List<PostEntity>> =
        postDao.observePosts(currentAreaId, type)

    fun observePost(postId: String): Flow<PostEntity?> = postDao.observePost(postId)

    fun observeComments(postId: String): Flow<List<CommentEntity>> = commentDao.observeForPost(postId)

    suspend fun refreshComments(postId: String): ApiResult<Unit> {
        if (localOnly) return ApiResult.Success(Unit)
        val result = safeApiCall { api.listComments(postId) }
        if (result is ApiResult.Success) {
            commentDao.upsertAll(
                result.data.map { dto ->
                    CommentEntity(
                        commentId = dto.commentId,
                        postId = dto.postId,
                        userId = dto.user.userId,
                        userName = dto.user.fullName,
                        body = dto.body,
                        createdAt = dto.createdAt,
                        syncState = "SYNCED",
                    )
                }
            )
        }
        return result.map { }
    }

    fun observeMyEvents(): Flow<List<PostEntity>> = postDao.observeMyEvents()
    fun observePendingSyncCount(): Flow<Int> = outboxDao.observePendingCount()
    fun observeNotifications(): Flow<List<NotificationLogEntity>> = notificationDao.observeAll()
    fun observeUnreadNotificationCount(): Flow<Int> = notificationDao.observeUnreadCount()
    suspend fun markNotificationsRead() = notificationDao.markAllRead()

    suspend fun refreshPosts(): ApiResult<Unit> {
        if (localOnly) return ApiResult.Success(Unit)
        val result = safeApiCall { api.listPosts(area = currentAreaId, limit = 50) }
        if (result is ApiResult.Success) {
            val incoming = result.data.items.map { it.toEntity() }
            postDao.upsertAll(incoming)
            Log.i(TAG, "Refreshed ${incoming.size} posts for area $currentAreaId")
        }
        return result.map { }
    }

    suspend fun createPost(draft: NewPostDraft): String {
        PostValidation.validate(draft)?.let { throw IllegalArgumentException(it) }

        val postId = UUID.randomUUID().toString()
        val now = Instant.now().toString()
        val userId = currentUserId ?: "unknown"
        val userName = tokenManager.fullName ?: "You"

        val entity = PostEntity(
            postId = postId,
            authorId = userId,
            authorName = userName,
            areaId = currentAreaId ?: "",
            categoryId = draft.categoryId ?: "",
            type = draft.type,
            title = draft.title.trim(),
            body = draft.body,
            imageUrl = null,
            status = "PUBLISHED",
            createdAt = now,
            updatedAt = now,
            eventStartsAt = draft.eventStartsAtIso,
            eventEndsAt = draft.eventEndsAtIso,
            eventVenueName = draft.eventVenueName,
            syncState = if (localOnly) "SYNCED" else "PENDING",
        )
        postDao.upsert(entity)

        if (!localOnly) {
            val createDto = PostCreateDto(
                type = draft.type,
                title = entity.title,
                body = entity.body,
                categoryId = draft.categoryId ?: "",
                event = if (draft.type == "EVENT" && draft.eventStartsAtIso != null) {
                    EventDetailInDto(
                        startsAt = draft.eventStartsAtIso,
                        endsAt = draft.eventEndsAtIso,
                        venueName = draft.eventVenueName,
                    )
                } else null,
                clientPostId = postId,
            )
            outboxDao.insert(
                OutboxEntity(
                    queueId = UUID.randomUUID().toString(),
                    entityName = "post",
                    operation = "CREATE",
                    payloadJson = json.encodeToString(PostCreateDto.serializer(), createDto),
                    createdAt = System.currentTimeMillis(),
                )
            )
            onOutboxChanged()
            logNotification("NEW_POST", "Draft queued", "\"${entity.title}\" will publish automatically once you're online.", postId)
        }
        Log.i(TAG, "Post $postId saved locally")
        return postId
    }

    suspend fun setRsvp(postId: String, response: String) {
        val post = postDao.getPost(postId) ?: return
        val previous = post.myRsvpResponse
        var going = post.rsvpGoing
        var interested = post.rsvpInterested
        var notGoing = post.rsvpNotGoing

        fun decrement(value: String?) = when (value) {
            "GOING" -> going--
            "INTERESTED" -> interested--
            "NOT_GOING" -> notGoing--
            else -> {}
        }
        fun increment(value: String) = when (value) {
            "GOING" -> going++
            "INTERESTED" -> interested++
            "NOT_GOING" -> notGoing++
            else -> {}
        }
        if (previous != response) {
            decrement(previous)
            increment(response)
        }

        postDao.updateRsvpCounts(postId, going, interested, notGoing, response)

        if (!localOnly) {
            outboxDao.insert(
                OutboxEntity(
                    queueId = UUID.randomUUID().toString(),
                    entityName = "rsvp",
                    operation = "UPDATE",
                    payloadJson = buildJsonObject {
                        put("post_id", postId)
                        put("response", response)
                    }.toString(),
                    createdAt = System.currentTimeMillis(),
                )
            )
            onOutboxChanged()
        }
    }

    suspend fun addComment(postId: String, body: String) {
        val commentId = UUID.randomUUID().toString()
        val now = Instant.now().toString()
        commentDao.upsert(
            CommentEntity(
                commentId = commentId,
                postId = postId,
                userId = currentUserId ?: "unknown",
                userName = tokenManager.fullName ?: "You",
                body = body,
                createdAt = now,
                syncState = if (localOnly) "SYNCED" else "PENDING",
            )
        )
        postDao.incrementCommentCount(postId)

        if (!localOnly) {
            outboxDao.insert(
                OutboxEntity(
                    queueId = UUID.randomUUID().toString(),
                    entityName = "comment",
                    operation = "CREATE",
                    payloadJson = buildJsonObject {
                        put("post_id", postId)
                        put("body", body)
                    }.toString(),
                    createdAt = System.currentTimeMillis(),
                )
            )
            onOutboxChanged()
        }
    }

    suspend fun exportMyData(): String {
        val uid = currentUserId
        val mine = postDao.observePosts(null, null).first().filter { it.authorId == uid }
        val s = settingsDao.get() ?: SettingsEntity()
        val root: JsonObject = buildJsonObject {
            put("exported_at", Instant.now().toString())
            put("user_id", uid)
            put("full_name", tokenManager.fullName)
            put("email", tokenManager.email)
            put("settings", buildJsonObject {
                put("language_code", s.languageCode)
                put("radius_km", s.radiusKm)
                put("push_opt_in", s.pushOptIn)
                put("sync_on_wifi_only", s.syncOnWifiOnly)
                put("biometric_unlock", s.biometricUnlock)
            })
            put("posts", buildJsonArray {
                mine.forEach { p ->
                    add(buildJsonObject {
                        put("post_id", p.postId)
                        put("type", p.type)
                        put("title", p.title)
                        put("body", p.body)
                        put("created_at", p.createdAt)
                        put("sync_state", p.syncState)
                    })
                }
            })
        }
        return Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), root)
    }

    // ----------------------------------------------------------------- sync

    suspend fun drainOutbox(): ApiResult<Unit> {
        if (localOnly) return ApiResult.Success(Unit)
        val pending = outboxDao.getAll()
        if (pending.isEmpty()) return ApiResult.Success(Unit)
        Log.i(TAG, "Draining outbox: ${pending.size} pending operation(s)")

        val settingsOps = pending.filter { it.entityName == "settings" }
        val batchOps = pending.filter { it.entityName != "settings" }

        for (op in settingsOps) {
            val update = json.decodeFromString(SettingsUpdateDto.serializer(), op.payloadJson)
            val result = safeApiCall { api.updateSettings(update) }
            if (result !is ApiResult.NetworkError) {
                outboxDao.delete(op.queueId)
            } else {
                return ApiResult.NetworkError
            }
        }

        val commentPosts = mutableSetOf<String>()
        if (batchOps.isNotEmpty()) {
            val operations = batchOps.map { op ->
                OutboxOperationDto(
                    queueId = op.queueId,
                    entityName = op.entityName,
                    operation = op.operation,
                    payload = json.parseToJsonElement(op.payloadJson),
                )
            }
            when (val result = safeApiCall { api.syncPush(SyncPushRequestDto(operations)) }) {
                is ApiResult.Success -> {
                    var accepted = 0
                    for (item in result.data.results) {
                        val original = batchOps.find { it.queueId == item.queueId }
                        outboxDao.delete(item.queueId)
                        when (item.status) {
                            "ACCEPTED" -> {
                                accepted++
                                if (original != null) onAccepted(original, item.serverId, commentPosts)
                            }
                            "REJECTED" -> if (original != null) onRejected(original, item.message)
                            else -> Log.w(TAG, "Unknown sync status '${item.status}'")
                        }
                    }
                    if (accepted > 0) {
                        logNotification("SYNC_COMPLETE", "Sync complete", "$accepted item(s) published.", notify = true)
                    }
                }
                is ApiResult.NetworkError -> return ApiResult.NetworkError
                is ApiResult.Error -> return result
            }
        }

        refreshPosts()
        for (postId in commentPosts) {
            refreshComments(postId)
            commentDao.deletePendingForPost(postId)
        }
        return ApiResult.Success(Unit)
    }

    private suspend fun onAccepted(op: OutboxEntity, serverId: String?, commentPosts: MutableSet<String>) {
        when (op.entityName) {
            "post" -> {
                val localId = payloadString(op, "client_post_id") ?: return
                if (serverId == null || serverId == localId) {
                    postDao.getPost(localId)?.let { postDao.update(it.copy(syncState = "SYNCED")) }
                } else {
                    postDao.delete(localId)
                }
            }
            "comment" -> payloadString(op, "post_id")?.let { commentPosts.add(it) }
        }
    }

    private suspend fun onRejected(op: OutboxEntity, message: String?) {
        Log.w(TAG, "Server rejected ${op.entityName}: $message")
        if (op.entityName == "post") {
            val title = payloadString(op, "title") ?: "Your post"
            payloadString(op, "client_post_id")?.let { postDao.delete(it) }
            logNotification(
                "SYNC_COMPLETE",
                "A post could not be published",
                "\"$title\": ${message ?: "The server rejected this change."}",
                notify = true,
            )
        } else {
            logNotification("SYNC_COMPLETE", "A change could not be saved", message ?: "The server rejected one of your changes.")
        }
    }

    private fun payloadString(op: OutboxEntity, key: String): String? = runCatching {
        json.parseToJsonElement(op.payloadJson).jsonObject[key]?.jsonPrimitive?.contentOrNull
    }.getOrNull()

    private suspend fun logNotification(
        type: String,
        title: String,
        body: String,
        relatedPostId: String? = null,
        notify: Boolean = false,
    ) {
        notificationDao.insert(
            NotificationLogEntity(
                notifId = UUID.randomUUID().toString(),
                type = type,
                title = title,
                body = body,
                relatedPostId = relatedPostId,
                createdAt = System.currentTimeMillis(),
            )
        )
        if (notify && (settingsDao.get()?.pushOptIn ?: true)) {
            onNotification(title, body)
        }
    }
}

private fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Error -> this
    is ApiResult.NetworkError -> this
}

private fun PostResponseDto.toEntity(): PostEntity = PostEntity(
    postId = postId,
    authorId = author.userId,
    authorName = author.fullName,
    areaId = areaId,
    categoryId = categoryId,
    type = type,
    title = title,
    body = body,
    imageUrl = imageUrl,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    eventStartsAt = event?.startsAt,
    eventEndsAt = event?.endsAt,
    eventVenueName = event?.venueName,
    eventLatitude = event?.latitude,
    eventLongitude = event?.longitude,
    rsvpGoing = rsvpCounts.going,
    rsvpInterested = rsvpCounts.interested,
    rsvpNotGoing = rsvpCounts.notGoing,
    myRsvpResponse = rsvpCounts.myResponse,
    commentCount = commentCount,
    syncState = "SYNCED",
)