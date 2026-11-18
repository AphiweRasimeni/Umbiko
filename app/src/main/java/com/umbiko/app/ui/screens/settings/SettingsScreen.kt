package com.umbiko.app.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.umbiko.app.R
import com.umbiko.app.di.LocalAppContainer
import com.umbiko.app.di.SimpleViewModelFactory

/** Language names are shown in their own language so users can always find theirs. */
private val LANGUAGES = listOf("en" to "English", "zu" to "isiZulu", "st" to "Sesotho", "af" to "Afrikaans")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onLoggedOut: () -> Unit) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SimpleViewModelFactory { SettingsViewModel(container.repository, container.tokenManager) },
    )
    val uiState by viewModel.uiState.collectAsState()
    var languageExpanded by remember { mutableStateOf(false) }
    val exportSubject = stringResource(R.string.export_subject)

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(uiState.fullName.ifBlank { "User" }, style = MaterialTheme.typography.titleMedium)
                    Text(uiState.email.ifBlank { "user@umbiko.app" }, style = MaterialTheme.typography.bodyMedium)
                }
                Button(
                    onClick = { viewModel.logout(onDone = onLoggedOut) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.settings_logout))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(expanded = languageExpanded, onExpandedChange = { languageExpanded = it }) {
                OutlinedTextField(
                    value = LANGUAGES.firstOrNull { it.first == uiState.settings.languageCode }?.second ?: "English",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                )
                ExposedDropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                    LANGUAGES.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.updateLanguage(code)
                                languageExpanded = false
                            },
                        )
                    }
                }
            }

            var radiusSliderValue by remember(uiState.settings.radiusKm) {
                mutableStateOf(uiState.settings.radiusKm.toFloat())
            }
            Text(
                stringResource(R.string.settings_radius) + ": ${radiusSliderValue.toInt()} km",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            Slider(
                value = radiusSliderValue,
                onValueChange = { radiusSliderValue = it },
                onValueChangeFinished = { viewModel.updateRadius(radiusSliderValue.toInt()) },
                valueRange = 1f..25f,
                steps = 23,
            )

            SettingsToggleRow(
                label = stringResource(R.string.settings_push),
                checked = uiState.settings.pushOptIn,
                onCheckedChange = viewModel::updatePushOptIn,
            )
            SettingsToggleRow(
                label = stringResource(R.string.settings_biometric),
                checked = uiState.settings.biometricUnlock,
                onCheckedChange = viewModel::updateBiometric,
            )
            SettingsToggleRow(
                label = stringResource(R.string.settings_sync_wifi),
                checked = uiState.settings.syncOnWifiOnly,
                onCheckedChange = viewModel::updateSyncOnWifiOnly,
            )

            uiState.offlineNotice?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            OutlinedButton(
                onClick = {
                    viewModel.exportData { json ->
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, exportSubject)
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(send, exportSubject))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_export))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
