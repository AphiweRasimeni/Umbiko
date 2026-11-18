package com.umbiko.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbiko.app.data.TokenManager
import com.umbiko.app.data.local.SettingsEntity
import com.umbiko.app.data.remote.SettingsUpdateDto
import com.umbiko.app.data.repository.UmbikoRepository
import com.umbiko.app.domain.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: SettingsEntity = SettingsEntity(),
    val fullName: String = "",
    val email: String = "",
    val savedMessageVisible: Boolean = false,
    val offlineNotice: String? = null,
)

/** Backs the Settings screen (FR-05). Changes go to Room first, then to the server. */
class SettingsViewModel(
    private val repository: UmbikoRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val savedMessageVisible = MutableStateFlow(false)
    private val offlineNotice = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.observeSettings(),
        savedMessageVisible,
        offlineNotice,
    ) { settings, saved, notice ->
        SettingsUiState(
            settings = settings ?: SettingsEntity(),
            fullName = tokenManager.fullName ?: "",
            email = tokenManager.email ?: "",
            savedMessageVisible = saved,
            offlineNotice = notice,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    init {
        viewModelScope.launch { repository.loadRemoteSettingsIntoCache() }
    }

    // The new language is applied app-wide by MainActivity, which observes saved settings.
    fun updateLanguage(languageCode: String) = apply(SettingsUpdateDto(languageCode = languageCode))
    fun updateRadius(radiusKm: Int) = apply(SettingsUpdateDto(radiusKm = radiusKm))
    fun updateBiometric(enabled: Boolean) = apply(SettingsUpdateDto(biometricUnlock = enabled))
    fun updateSyncOnWifiOnly(enabled: Boolean) = apply(SettingsUpdateDto(syncOnWifiOnly = enabled))
    fun updatePushOptIn(enabled: Boolean) = apply(SettingsUpdateDto(pushOptIn = enabled))

    private fun apply(update: SettingsUpdateDto) {
        viewModelScope.launch {
            val result = repository.updateSettings(update)
            savedMessageVisible.value = true
            offlineNotice.value = if (result is ApiResult.NetworkError) {
                "Saved on this device — will sync once you're back online."
            } else {
                null
            }
        }
    }

    /** Builds the JSON export and hands it to the UI (which opens the share sheet). */
    fun exportData(onReady: (String) -> Unit) {
        viewModelScope.launch { onReady(repository.exportMyData()) }
    }

    /** [onDone] runs after local data is wiped, so navigation can't cancel the wipe. */
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onDone()
        }
    }
}
