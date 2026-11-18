package com.umbiko.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbiko.app.data.remote.AreaResponseDto
import com.umbiko.app.data.repository.UmbikoRepository
import com.umbiko.app.domain.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val areas: List<AreaResponseDto> = AuthViewModel.DEFAULT_AREAS,
    val areasLoading: Boolean = false,
)

/** Backs both Register and Login (FR-01, FR-02), including Google single sign-on. */
class AuthViewModel(private val repository: UmbikoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun loadAreas() {
        if (_uiState.value.areasLoading) return
        _uiState.value = _uiState.value.copy(areasLoading = true)
        viewModelScope.launch {
            when (val result = repository.listAreas()) {
                is ApiResult.Success -> {
                    val list = if (result.data.isNotEmpty()) result.data else DEFAULT_AREAS
                    _uiState.value = _uiState.value.copy(areas = list, areasLoading = false)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(areas = DEFAULT_AREAS, areasLoading = false)
                }
                ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(areas = DEFAULT_AREAS, areasLoading = false)
                }
            }
        }
    }

    fun register(fullName: String, email: String, mobile: String, areaId: String, password: String) {
        val validationError = AuthValidation.validateRegistration(fullName, email, mobile, areaId, password)
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(errorMessage = validationError)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.register(fullName.trim(), email.trim(), mobile.trim(), areaId, password)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (result.message.isBlank()) "Server error (${result.code}). Please try again." else result.message
                )
                ApiResult.NetworkError -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unable to connect to server. Please check your internet connection.",
                )
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter your email and password")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.login(email.trim(), password)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                ApiResult.NetworkError -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No connection. Please try again once you're back online.",
                )
            }
        }
    }

    /** SSO: exchange a Google ID token for an Umbiko session. */
    fun loginWithGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.loginWithGoogle(idToken)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                ApiResult.NetworkError -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No connection. Please try again once you're back online.",
                )
            }
        }
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    companion object {
        val DEFAULT_AREAS = listOf(
            AreaResponseDto(areaId = "area-1", name = "Soweto", code = "SWT"),
            AreaResponseDto(areaId = "area-2", name = "Johannesburg CBD", code = "JHB"),
            AreaResponseDto(areaId = "area-3", name = "Sandton", code = "SDN"),
            AreaResponseDto(areaId = "area-4", name = "Pretoria / Tshwane", code = "PTA"),
            AreaResponseDto(areaId = "area-5", name = "Cape Town Central", code = "CPT"),
            AreaResponseDto(areaId = "area-6", name = "Durban / eThekwini", code = "DBN"),
        )
    }
}

/**
 * Client-side registration validation (FR-01), a dependency-free object so it can be
 * unit tested with plain JUnit. Mirrors the server checks (app/schemas.py); the server
 * stays the source of truth and re-validates.
 */
object AuthValidation {
    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val MOBILE_REGEX = Regex("^(0|\\+27)\\d{9}$")

    fun validateRegistration(fullName: String, email: String, mobile: String, areaId: String, password: String): String? {
        if (fullName.trim().length !in 2..80) return "Enter your full name"
        if (!EMAIL_REGEX.matches(email.trim())) return "Enter a valid email address"
        if (!MOBILE_REGEX.matches(mobile.trim())) return "Enter a valid South African mobile number"
        if (areaId.isBlank()) return "Choose your area"
        if (password.length < 8 || !password.any { it.isUpperCase() } || !password.any { it.isDigit() } ||
            password.none { !it.isLetterOrDigit() }
        ) {
            return "Password needs 8+ characters, an uppercase letter, a digit and a symbol"
        }
        return null
    }
}
