package com.umbiko.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbiko.app.data.local.PostEntity
import com.umbiko.app.data.repository.UmbikoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class HomeUiState(
    val posts: List<PostEntity> = emptyList(),
    val filterType: String? = null,
    val pendingSyncCount: Int = 0,
    val unreadNotifCount: Int = 0,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

class HomeViewModel(private val repository: UmbikoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
        refresh()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.observePosts(_uiState.value.filterType).collectLatest { posts ->
                _uiState.value = _uiState.value.copy(posts = posts)
            }
        }
        viewModelScope.launch {
            repository.observePendingSyncCount().collectLatest { count ->
                _uiState.value = _uiState.value.copy(pendingSyncCount = count)
            }
        }
        viewModelScope.launch {
            repository.observeUnreadNotificationCount().collectLatest { count ->
                _uiState.value = _uiState.value.copy(unreadNotifCount = count)
            }
        }
    }

    fun setFilter(type: String?) {
        _uiState.value = _uiState.value.copy(filterType = type)
        viewModelScope.launch {
            repository.observePosts(type).collectLatest { posts ->
                _uiState.value = _uiState.value.copy(posts = posts)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            repository.refreshPosts()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun rsvp(postId: String, response: String) {
        viewModelScope.launch {
            repository.setRsvp(postId, response)
        }
    }
}
