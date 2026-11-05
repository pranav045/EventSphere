
package com.example.eventsphere.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventsphere.data.CampusEvent

@Composable
fun HomeScreen(
    events: List<CampusEvent> = emptyList(),
    registeredIds: List<String> = emptyList(),
    isAdmin: Boolean = false,
    onEventClick: (CampusEvent) -> Unit = {},
    onCreateClick: () -> Unit = {},
    onMyEventsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAdminClick: () -> Unit = {}
) {
    var search by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("All")
    }

    val categories = listOf(
        "All",
        "Technology",
        "Cultural",
        "Sports",
        "Workshop",
        "Academic",
        "Other"
    )

    val filtered = events.filter { event ->
        (category == "All" ||
                event.category.equals(category, ignoreCase = true)) &&
                (
                        event.title.contains(search, ignoreCase = true) ||
                                event.description.contains(search, ignoreCase = true) ||
                                event.venue.contains(search, ignoreCase = true)
                        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = {
                Text("Search campus events")
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null
                )
            },
            singleLine = true
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(categories) { item ->
                FilterChip(
                    selected = category == item,
                    onClick = {
                        category = item
                    },
                    label = {
                        Text(item)
                    }
                )
            }
        }

        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${filtered.size} events",
                style = MaterialTheme.typography.titleMedium
            )

            if (isAdmin) {
                Button(
                    onClick = onCreateClick
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(6.dp))

                    Text("Create event")
                }
            }
        }

        if (filtered.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No events found. Try another search.")
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                items(
                    items = filtered,
                    key = { it.id }
                ) { event ->

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onEventClick(event)
                            }
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                event.category,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                event.title,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                event.description,
                                maxLines = 3,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(10.dp))

                            Text("📍 ${event.venue}")

                            Text("📅 ${event.date}  ${event.time}")

                            if (event.id in registeredIds) {
                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "You're registered",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
