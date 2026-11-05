package com.example.eventsphere.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSphereScaffold(
    title: String,
    selectedRoute: String,
    isAdmin: Boolean,
    onNavigate: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedRoute == "HOME",
                    onClick = { onNavigate("HOME") },
                    icon = {
                        Icon(Icons.Default.Event, contentDescription = "Home")
                    },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedRoute == "MY_EVENTS",
                    onClick = { onNavigate("MY_EVENTS") },
                    icon = {
                        Icon(
                            Icons.Default.EventNote,
                            contentDescription = "My Events"
                        )
                    },
                    label = { Text("My Events") }
                )

                if (isAdmin) {
                    NavigationBarItem(
                        selected = selectedRoute == "ADMIN",
                        onClick = { onNavigate("ADMIN") },
                        icon = {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin"
                            )
                        },
                        label = { Text("Admin") }
                    )
                }

                NavigationBarItem(
                    selected = selectedRoute == "PROFILE",
                    onClick = { onNavigate("PROFILE") },
                    icon = {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            content(innerPadding)
        }
    }
}