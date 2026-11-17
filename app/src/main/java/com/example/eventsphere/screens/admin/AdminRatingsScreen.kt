
package com.example.eventsphere.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class AdminRating(
    val id: String,
    val eventTitle: String,
    val userId: String,
    val rating: Int,
    val feedback: String,
    val timestamp: Long
)

private fun getSafeTimestamp(value: Any?): Long {
    return when (value) {
        is Timestamp -> value.toDate().time
        is Date -> value.time
        is Number -> value.toLong()
        is String -> value.toLongOrNull() ?: 0L
        else -> 0L
    }
}

@Composable
fun AdminRatingsScreen(
    isAdmin: Boolean
) {
    var ratings by remember {
        mutableStateOf<List<AdminRating>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    DisposableEffect(isAdmin) {
        var listener: ListenerRegistration? = null

        if (isAdmin) {
            listener = FirebaseFirestore.getInstance()
                .collection("ratings")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        errorMessage = error.message
                            ?: "Failed to load ratings"
                        isLoading = false
                    } else {
                        ratings = snapshot?.documents?.map { doc ->

                            val timestamp = getSafeTimestamp(
                                doc.get("timestamp")
                            )

                            AdminRating(
                                id = doc.id,
                                eventTitle = doc.getString("eventTitle")
                                    .orEmpty(),
                                userId = doc.getString("userId")
                                    .orEmpty(),
                                rating = (doc.getLong("rating") ?: 0L)
                                    .toInt(),
                                feedback = doc.getString("feedback")
                                    .orEmpty(),
                                timestamp = timestamp
                            )

                        }?.sortedByDescending {
                            it.timestamp
                        }.orEmpty()

                        errorMessage = ""
                        isLoading = false
                    }
                }
        } else {
            isLoading = false
        }

        onDispose {
            listener?.remove()
        }
    }

    val averageRating = if (ratings.isNotEmpty()) {
        ratings.map { it.rating }.average()
    } else {
        0.0
    }

    if (!isAdmin) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Access denied",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                text = "Student Ratings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Total Ratings")

                    Text(
                        text = ratings.size.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = " ${"%.1f".format(Locale.getDefault(), averageRating)} / 5 Average Rating",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "All Feedback",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (isLoading) {
            item {
                CircularProgressIndicator()
            }
        }

        if (errorMessage.isNotEmpty()) {
            item {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (!isLoading && errorMessage.isEmpty() && ratings.isEmpty()) {
            item {
                Text("No ratings have been submitted yet.")
            }
        }

        items(
            items = ratings,
            key = { it.id }
        ) { rating ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = rating.eventTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < rating.rating) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        }

                        Text(
                            text = "  ${rating.rating}/5",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Student ID: ${rating.userId}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = if (rating.feedback.isNotBlank()) {
                            rating.feedback
                        } else {
                            "No written feedback provided."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (rating.timestamp > 0L) {
                        Text(
                            text = "Submitted: ${
                                SimpleDateFormat(
                                    "dd MMM yyyy, hh:mm a",
                                    Locale.getDefault()
                                ).format(Date(rating.timestamp))
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Submission date unavailable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
