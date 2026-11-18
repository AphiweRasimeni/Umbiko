package com.umbiko.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.umbiko.app.R
import com.umbiko.app.data.local.PostEntity
import com.umbiko.app.di.LocalAppContainer
import com.umbiko.app.di.SimpleViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(factory = SimpleViewModelFactory { HomeViewModel(container.repository) })
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(badge = {
                            if (uiState.unreadNotifCount > 0) {
                                Badge { Text("${uiState.unreadNotifCount}") }
                            }
                        }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreatePost) {
                Icon(Icons.Filled.Add, contentDescription = "New Post")
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (uiState.pendingSyncCount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.offline_banner, uiState.pendingSyncCount),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = when (uiState.filterType) {
                        "NOTICE" -> 1
                        "EVENT" -> 2
                        else -> 0
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Tab(
                        selected = uiState.filterType == null,
                        onClick = { viewModel.setFilter(null) },
                        text = { Text(stringResource(R.string.filter_all)) },
                    )
                    Tab(
                        selected = uiState.filterType == "NOTICE",
                        onClick = { viewModel.setFilter("NOTICE") },
                        text = { Text(stringResource(R.string.filter_notices)) },
                    )
                    Tab(
                        selected = uiState.filterType == "EVENT",
                        onClick = { viewModel.setFilter("EVENT") },
                        text = { Text(stringResource(R.string.filter_events)) },
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    if (uiState.posts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.empty_feed_title),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.empty_feed_body),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { viewModel.refresh() }) {
                                    Text(stringResource(R.string.action_retry))
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item { Spacer(Modifier.height(8.dp)) }
                            items(uiState.posts) { post ->
                                PostCard(
                                    post = post,
                                    onClick = { onNavigateToPostDetail(post.postId) },
                                    onRsvp = { response -> viewModel.rsvp(post.postId, response) },
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: PostEntity,
    onClick: () -> Unit,
    onRsvp: (String) -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = post.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
            )
            if (post.type == "EVENT") {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { onRsvp("GOING") },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.rsvp_going) + " (${post.rsvpGoing})")
                    }
                    Button(
                        onClick = { onRsvp("INTERESTED") },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.rsvp_interested) + " (${post.rsvpInterested})")
                    }
                }
            }
        }
    }
}
