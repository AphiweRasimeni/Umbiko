package com.umbiko.app.domain

sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>
    data class Error(val message: String, val code: Int = 0) : ApiResult<Nothing>
    data object NetworkError : ApiResult<Nothing>
}
