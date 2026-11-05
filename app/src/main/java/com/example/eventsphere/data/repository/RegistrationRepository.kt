
package com.example.eventsphere.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object RegistrationRepository {

    private const val TAG = "EVENT_REGISTER"

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun registerForEvent(
        eventId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            callback(false, "Please log in first.")
            return
        }

        val uid = user.uid

        val registrationId = "${uid}_${eventId}"

        val registrationRef = db.collection("registrations")
            .document(registrationId)

        val eventRef = db.collection("events")
            .document(eventId)

        Log.e(TAG, "Starting registration: $registrationId")

        db.runTransaction { transaction ->

            val registration = transaction.get(registrationRef)
            val event = transaction.get(eventRef)

            if (registration.exists()) {
                throw IllegalStateException(
                    "You are already registered."
                )
            }

            if (!event.exists()) {
                throw IllegalStateException(
                    "This event no longer exists."
                )
            }

            val registrationData = mapOf(
                "userId" to uid,
                "eventId" to eventId,
                "checkedIn" to false,
                "registeredAt" to FieldValue.serverTimestamp()
            )

            transaction.set(
                registrationRef,
                registrationData
            )

            val count = event.getLong("attendeeCount") ?: 0L

            transaction.update(
                eventRef,
                "attendeeCount",
                count + 1
            )

            true

        }.addOnSuccessListener {

            Log.e(TAG, "Registration successful: $eventId")

            callback(
                true,
                "Registration successful!"
            )

        }.addOnFailureListener { error ->

            Log.e(TAG, "Registration failed", error)

            callback(
                false,
                error.localizedMessage ?: "Registration failed."
            )
        }
    }

    fun cancelRegistration(
        eventId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            callback(false, "Please log in first.")
            return
        }

        val uid = user.uid

        val registrationRef = db.collection("registrations")
            .document("${uid}_${eventId}")

        val eventRef = db.collection("events")
            .document(eventId)

        db.runTransaction { transaction ->

            val registration = transaction.get(registrationRef)
            val event = transaction.get(eventRef)

            if (!registration.exists()) {
                throw IllegalStateException(
                    "You are not registered for this event."
                )
            }

            if (registration.getBoolean("checkedIn") == true) {
                throw IllegalStateException(
                    "You cannot cancel after check-in."
                )
            }

            if (registration.getString("userId") != uid) {
                throw IllegalStateException(
                    "You cannot cancel another user's registration."
                )
            }

            transaction.delete(registrationRef)

            if (event.exists()) {
                val count = event.getLong("attendeeCount") ?: 0L

                transaction.update(
                    eventRef,
                    "attendeeCount",
                    (count - 1).coerceAtLeast(0L)
                )
            }

            true

        }.addOnSuccessListener {

            Log.e(TAG, "Registration cancelled: $eventId")

            callback(
                true,
                "Registration cancelled."
            )

        }.addOnFailureListener { error ->

            Log.e(TAG, "Cancellation failed", error)

            callback(
                false,
                error.localizedMessage ?: "Cancellation failed."
            )
        }
    }

    fun getRegisteredEventIds(
        callback: (List<String>) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            callback(emptyList())
            return
        }

        db.collection("registrations")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val eventIds = snapshot.documents.mapNotNull {
                    it.getString("eventId")
                }

                callback(eventIds)
            }
            .addOnFailureListener { error ->

                Log.e(TAG, "Failed to load registered events", error)

                callback(emptyList())
            }
    }
}
