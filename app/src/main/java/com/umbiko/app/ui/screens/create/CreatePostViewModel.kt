package com.umbiko.app.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbiko.app.data.ConnectivityObserver
import com.umbiko.app.data.remote.CategoryResponseDto
import com.umbiko.app.data.repository.UmbikoRepository
import com.umbiko.app.domain.ApiResult
import com.umbiko.app.domain.NewPostDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreatePostUiState(
    val categories: List<CategoryResponseDto> = emptyList(),
    val isLoadingCategories: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

class CreatePostViewModel(
    private val repository: UmbikoRepository,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun loadCategories() {
        if (_uiState.value.categories.isNotEmpty() || _uiState.value.isLoadingCategories) return
        _uiState.value = _uiState.value.copy(isLoadingCategories = true)
        viewModelScope.launch {
            when (val result = repository.listCategories()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    categories = result.data,
                    isLoadingCategories = false,
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoadingCategories = false,
                    errorMessage = result.message,
                )
                ApiResult.NetworkError -> _uiState.value = _uiState.value.copy(
                    isLoadingCategories = false,
                    errorMessage = "Couldn't load categories — check connection.",
                )
            }
        }
    }

    fun publish(draft: NewPostDraft) {
        _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            try {
                repository.createPost(draft)
                _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = e.message ?: "Failed to publish post",
                )
            }
        }
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(isSuccess = false, errorMessage = null)
    }
}
