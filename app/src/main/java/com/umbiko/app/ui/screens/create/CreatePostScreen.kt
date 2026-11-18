package com.umbiko.app.ui.screens.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.umbiko.app.R
import com.umbiko.app.di.LocalAppContainer
import com.umbiko.app.di.SimpleViewModelFactory
import com.umbiko.app.domain.NewPostDraft
import com.umbiko.app.ui.components.ErrorBanner
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

/** "safety_alerts" -> "Safety alerts" (category names arrive as keys from the API). */
private fun prettyCategory(nameKey: String): String =
    nameKey.replace('_', ' ').replace('.', ' ').trim().replaceFirstChar { it.uppercase() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(onPublished: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: CreatePostViewModel = viewModel(
        factory = SimpleViewModelFactory { CreatePostViewModel(container.repository, container.connectivityObserver) },
    )
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var type by remember { mutableStateOf("NOTICE") }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<String?>(null) }
    var selectedDateTime by remember { mutableStateOf<ZonedDateTime?>(null) }

    LaunchedEffect(type) {
        if (type == "EVENT" && selectedDateTime == null) {
            selectedDateTime = ZonedDateTime.now().plusDays(1).withHour(10).withMinute(0)
        }
    }

    LaunchedEffect(Unit) { viewModel.loadCategories() }
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            title = ""; body = ""; venue = ""; categoryId = null; selectedDateTime = null
            onPublished()
            viewModel.reset()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.create_post_title)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Row {
                FilterChip(
                    selected = type == "NOTICE",
                    onClick = { type = "NOTICE" },
                    label = { Text(stringResource(R.string.type_notice)) },
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = type == "EVENT",
                    onClick = { type = "EVENT" },
                    label = { Text(stringResource(R.string.type_event)) },
                )
            }

            if (uiState.categories.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.field_category), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.categories.forEach { category ->
                        FilterChip(
                            selected = categoryId == category.categoryId,
                            onClick = {
                                categoryId = if (categoryId == category.categoryId) null else category.categoryId
                            },
                            label = { Text(prettyCategory(category.nameKey)) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.field_title)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = body,
                onValueChange = { if (it.length <= 1000) body = it },
                label = { Text(stringResource(R.string.field_body)) },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                supportingText = { Text("${body.length}/1000") },
            )

            if (type == "EVENT") {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text(stringResource(R.string.field_venue)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        selectedDateTime?.let {
                            calendar.timeInMillis = it.toInstant().toEpochMilli()
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        calendar.set(year, month, dayOfMonth, hour, minute)
                                        selectedDateTime = ZonedDateTime.ofInstant(
                                            Instant.ofEpochMilli(calendar.timeInMillis),
                                            ZoneId.systemDefault(),
                                        )
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true,
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        selectedDateTime?.format(DateTimeFormatter.ofPattern("EEE d MMM, HH:mm"))
                            ?: stringResource(R.string.field_date_time)
                    )
                }
            }

            uiState.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                ErrorBanner(it)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.publish(
                        NewPostDraft(
                            type = type,
                            title = title,
                            body = body,
                            categoryId = categoryId,
                            eventStartsAtIso = if (type == "EVENT") selectedDateTime?.toInstant()?.toString() else null,
                            eventVenueName = if (type == "EVENT" && venue.isNotBlank()) venue else null,
                        )
                    )
                },
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.action_publish))
                }
            }
        }
    }
}
