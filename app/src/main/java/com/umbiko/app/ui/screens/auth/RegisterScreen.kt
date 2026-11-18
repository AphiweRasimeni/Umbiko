package com.umbiko.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.umbiko.app.R
import com.umbiko.app.data.remote.AreaResponseDto
import com.umbiko.app.di.LocalAppContainer
import com.umbiko.app.di.SimpleViewModelFactory
import com.umbiko.app.ui.components.ErrorBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: AuthViewModel = viewModel(factory = SimpleViewModelFactory { AuthViewModel(container.repository) })
    val uiState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf<AreaResponseDto?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var areaExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAreas()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegistered()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp),
        )

        Text(stringResource(R.string.register_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; viewModel.clearError() },
            label = { Text(stringResource(R.string.field_full_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearError() },
            label = { Text(stringResource(R.string.field_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it; viewModel.clearError() },
            label = { Text(stringResource(R.string.field_mobile)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = areaExpanded,
            onExpandedChange = { areaExpanded = !areaExpanded },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = selectedArea?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.field_area)) },
                placeholder = { Text("Select your area") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = areaExpanded,
                onDismissRequest = { areaExpanded = false },
            ) {
                uiState.areas.forEach { area ->
                    DropdownMenuItem(
                        text = { Text(area.name) },
                        onClick = {
                            selectedArea = area
                            areaExpanded = false
                            viewModel.clearError()
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearError() },
            label = { Text(stringResource(R.string.field_password)) },
            supportingText = { Text(stringResource(R.string.password_hint)) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        uiState.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            ErrorBanner(it)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                viewModel.register(
                    fullName = fullName,
                    email = email,
                    mobile = mobile,
                    areaId = selectedArea?.areaId ?: "",
                    password = password,
                )
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.action_register))
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onNavigateToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.prompt_have_account))
        }
    }
}
