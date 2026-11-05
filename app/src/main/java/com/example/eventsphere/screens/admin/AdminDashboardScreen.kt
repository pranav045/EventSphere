
package com.example.eventsphere.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eventsphere.data.CampusEvent
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AdminDashboardScreen(
    events: List<CampusEvent>,
    onBack: () -> Unit,
    onCreateEvent: () -> Unit,
    onEventClick: (CampusEvent) -> Unit,
    onEditEvent: (CampusEvent) -> Unit,
    onScanQr: () -> Unit,
    isAdmin: Boolean
) {
    var firestoreEventCount by remember {
        mutableStateOf(events.size)
    }

    var registrationCount by remember {
        mutableStateOf(0)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    fun refreshDashboard() {
        isLoading = true

        FirebaseFirestore.getInstance()
            .collection("events")
            .get()
            .addOnSuccessListener { snapshot ->
                firestoreEventCount = snapshot.size()
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }

        FirebaseFirestore.getInstance()
            .collection("registrations")
            .get()
            .addOnSuccessListener { snapshot ->
                registrationCount = snapshot.size()
            }
    }

    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            refreshDashboard()
        } else {
            isLoading = false
        }
    }

    Scaffold { innerPadding ->

        if (!isAdmin) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Access denied",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text("Administrator permissions are required.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null
                                )

                                Text("Events")

                                Text(
                                    text = firestoreEventCount.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null
                                )

                                Text("Registrations")

                                Text(
                                    text = registrationCount.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = onCreateEvent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Text(" Create Event")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onScanQr,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null
                        )
                        Text(" Scan QR Ticket")
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Manage Events",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { refreshDashboard() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh"
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        CircularProgressIndicator()
                    }
                }

                if (events.isEmpty()) {
                    item {
                        Text("No events found. Create your first event.")
                    }
                } else {
                    items(
                        items = events,
                        key = { it.id }
                    ) { event ->

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text("Category: ${event.category}")
                                Text("Date: ${event.date}")
                                Text("Venue: ${event.venue}")
                                Text("Attendees: ${event.attendeeCount}")

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onEventClick(event) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Details")
                                    }

                                    Button(
                                        onClick = { onEditEvent(event) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Edit")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
