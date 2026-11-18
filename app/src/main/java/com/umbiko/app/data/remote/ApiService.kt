package com.umbiko.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit contract for the Umbiko REST API. Authentication is attached to every
 * call by [AuthInterceptor] (bearer token), so endpoints need no Authorization param.
 */
interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): TokenResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): TokenResponseDto

    /** Single sign-on: exchange a Google ID token for Umbiko tokens. */
    @POST("auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequestDto): TokenResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): RefreshResponseDto

    @GET("users/me/settings")
    suspend fun getSettings(): SettingsResponseDto

    @PUT("users/me/settings")
    suspend fun updateSettings(@Body body: SettingsUpdateDto): SettingsResponseDto

    @POST("users/me/device-token")
    suspend fun registerDeviceToken(@Body body: DeviceTokenDto): Response<Unit>

    @GET("areas")
    suspend fun listAreas(@Query("q") query: String? = null): List<AreaResponseDto>

    @GET("categories")
    suspend fun listCategories(): List<CategoryResponseDto>

    @POST("posts")
    suspend fun createPost(@Body body: PostCreateDto): PostResponseDto

    @GET("posts")
    suspend fun listPosts(
        @Query("area") area: String? = null,
        @Query("category") category: String? = null,
        @Query("type") type: String? = null,
        @Query("since") since: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
    ): PostListResponseDto

    @GET("posts/{postId}")
    suspend fun getPost(@Path("postId") postId: String): PostResponseDto

    @PUT("posts/{postId}")
    suspend fun updatePost(@Path("postId") postId: String, @Body body: PostUpdateDto): PostResponseDto

    @DELETE("posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: String): Response<Unit>

    @POST("posts/{postId}/rsvp")
    suspend fun rsvp(@Path("postId") postId: String, @Body body: RsvpRequestDto): RsvpCountsDto

    @GET("posts/{postId}/comments")
    suspend fun listComments(@Path("postId") postId: String): List<CommentResponseDto>

    @POST("posts/{postId}/comments")
    suspend fun addComment(@Path("postId") postId: String, @Body body: CommentCreateDto): CommentResponseDto

    @POST("sync/push")
    suspend fun syncPush(@Body body: SyncPushRequestDto): SyncPushResponseDto

    @GET("sync/pull")
    suspend fun syncPull(@Query("since") since: String? = null): SyncPullResponseDto
}
