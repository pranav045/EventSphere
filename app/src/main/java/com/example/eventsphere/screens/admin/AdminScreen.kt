
package com.example.eventsphere.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventsphere.data.CampusEvent

@Composable
fun AdminScreen(
    events: List<CampusEvent>,
    onCreate: () -> Unit,
    onEdit: (CampusEvent) -> Unit,
    onDelete: (CampusEvent) -> Unit,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Total events: ${events.size}")

        Text(
            "Total registrations: ${events.sumOf { it.attendeeCount }}"
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create event")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(events, key = { it.id }) { event ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            event.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("${event.category} · ${event.date}")

                        Text("Attendees: ${event.attendeeCount}")

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { onEdit(event) }
                            ) {
                                Text("Edit")
                            }

                            TextButton(
                                onClick = { onDelete(event) }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
