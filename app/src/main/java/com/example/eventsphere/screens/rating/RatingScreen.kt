
package com.example.eventsphere.screens.rating

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    eventId: String,
    eventTitle: String,
    onBack: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val uid = auth.currentUser?.uid

    var rating by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var alreadyRated by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val ratingId = if (uid != null && eventId.isNotBlank()) {
        "${uid}_${eventId}"
    } else {
        ""
    }

    LaunchedEffect(ratingId) {
        if (uid == null) {
            message = "Please login to rate this event."
            isLoading = false
            return@LaunchedEffect
        }

        if (eventId.isBlank()) {
            message = "Invalid event ID."
            isLoading = false
            return@LaunchedEffect
        }

        Log.d("RatingDebug", "User ID: $uid")
        Log.d("RatingDebug", "Event ID: $eventId")
        Log.d("RatingDebug", "Rating document ID: $ratingId")

        db.collection("ratings")
            .document(ratingId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("RatingDebug", "Document exists: ${document.exists()}")

                if (document.exists()) {
                    alreadyRated = true
                    rating = (document.getLong("rating") ?: 0L).toInt()
                    feedback = document.getString("feedback") ?: ""
                }

                isLoading = false
            }
            .addOnFailureListener { error ->
                Log.e("RatingDebug", "Rating read failed", error)
                message = error.localizedMessage ?: "Unable to load rating."
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate Event") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = eventTitle,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "How was your experience?",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                uid == null || eventId.isBlank() -> {
                    Text(message)
                }

                alreadyRated -> {
                    Text(
                        text = "You have already rated this event.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < rating) {
                                    Icons.Filled.Star
                                } else {
                                    Icons.Outlined.Star
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Your feedback: $feedback")
                }

                else -> {
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = {
                                    rating = index + 1
                                }
                            ) {
                                Icon(
                                    imageVector = if (index < rating) {
                                        Icons.Filled.Star
                                    } else {
                                        Icons.Outlined.Star
                                    },
                                    contentDescription = "${index + 1} stars",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        text = if (rating == 0) {
                            "Select your rating"
                        } else {
                            "$rating out of 5 stars"
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("Write your feedback") },
                        placeholder = {
                            Text("Share your experience...")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (rating == 0) {
                                message = "Please select a star rating."
                                return@Button
                            }

                            if (uid == null || ratingId.isBlank()) {
                                message = "Please login and try again."
                                return@Button
                            }

                            isSubmitting = true
                            message = ""

                            val ratingData = hashMapOf(
                                "userId" to uid,
                                "eventId" to eventId,
                                "eventTitle" to eventTitle,
                                "rating" to rating,
                                "feedback" to feedback.trim(),
                                "timestamp" to FieldValue.serverTimestamp()
                            )

                            db.collection("ratings")
                                .document(ratingId)
                                .set(ratingData)
                                .addOnSuccessListener {
                                    Log.d(
                                        "RatingDebug",
                                        "Rating submitted successfully"
                                    )
                                    alreadyRated = true
                                    isSubmitting = false
                                    message = "Thank you for your feedback!"
                                }
                                .addOnFailureListener { error ->
                                    Log.e(
                                        "RatingDebug",
                                        "Rating submission failed",
                                        error
                                    )
                                    isSubmitting = false
                                    message = error.localizedMessage
                                        ?: "Failed to submit rating."
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator()
                        } else {
                            Text("Submit Rating")
                        }
                    }
                }
            }

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

