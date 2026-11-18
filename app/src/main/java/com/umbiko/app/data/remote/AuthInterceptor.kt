package com.umbiko.app.data.remote

import com.umbiko.app.data.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches "Authorization: Bearer <token>" to every outgoing request except the
 * endpoints that must work without one (register, login, Google SSO, refresh).
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    private val publicPaths = listOf("/auth/register", "/auth/login", "/auth/google", "/auth/refresh", "/")

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath
        val isPublic = publicPaths.any { path.endsWith(it) }

        if (isPublic) {
            return chain.proceed(original)
        }

        val token = tokenManager.accessToken
        val requestBuilder = original.newBuilder()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
