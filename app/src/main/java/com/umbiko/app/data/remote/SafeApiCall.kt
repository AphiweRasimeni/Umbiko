package com.umbiko.app.data.remote

import com.umbiko.app.domain.ApiResult
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

private val json = Json { ignoreUnknownKeys = true }

suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(call())
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val message = try {
            errorBody?.let { json.decodeFromString(ApiErrorDto.serializer(), it).message ?: errorBody }
                ?: e.message()
        } catch (_: Exception) {
            e.message() ?: "Server error (${e.code()})"
        }
        ApiResult.Error(message = message, code = e.code())
    } catch (_: IOException) {
        ApiResult.NetworkError
    } catch (e: Exception) {
        ApiResult.Error(message = e.message ?: "An unexpected error occurred")
    }
}
