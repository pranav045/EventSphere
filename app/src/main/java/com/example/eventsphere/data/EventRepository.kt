
package com.example.eventsphere.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object EventRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun observeEvents(
        onResult: (List<CampusEvent>) -> Unit
    ) {
        db.collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot.documents.mapNotNull { document ->
                    document.toObject(CampusEvent::class.java)
                        ?.copy(id = document.id)
                }

                onResult(events)
            }
    }

    fun saveEvent(
        event: CampusEvent,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResult(false, "Please sign in first.")
            return
        }

        val eventData = event.copy(
            ownerId = if (event.ownerId.isBlank()) uid else event.ownerId
        )

        val document = if (event.id.isBlank()) {
            db.collection("events").document()
        } else {
            db.collection("events").document(event.id)
        }

        document.set(eventData.copy(id = document.id))
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }

    fun deleteEvent(
        eventId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (auth.currentUser == null) {
            onResult(false, "Please sign in first.")
            return
        }

        db.collection("events")
            .document(eventId)
            .delete()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage)
            }
    }



    fun registerForEvent(
        eventId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResult(false, "Please sign in first.")
            return
        }

        android.util.Log.e("EVENT_REGISTER", "STEP 1: Function called")

        val eventRef = db.collection("events").document(eventId)
        val userRef = db.collection("users").document(uid)
        val registrationRef = db.collection("registrations")
            .document("${uid}_${eventId}")

        db.runTransaction { transaction ->

            android.util.Log.e("EVENT_REGISTER", "STEP 2: Reading event")

            val eventSnapshot = transaction.get(eventRef)

            android.util.Log.e("EVENT_REGISTER", "STEP 3: Reading user")

            val userSnapshot = transaction.get(userRef)

            android.util.Log.e("EVENT_REGISTER", "STEP 4: Reading registration")

            val registrationSnapshot = transaction.get(registrationRef)

            android.util.Log.e("EVENT_REGISTER", "STEP 5: All reads completed")

            if (!eventSnapshot.exists()) {
                throw Exception("Event not found.")
            }

            if (registrationSnapshot.exists()) {
                return@runTransaction false
            }

            val registeredEvents =
                userSnapshot.get("registeredEvents") as? List<String>
                    ?: emptyList()

            if (eventId in registeredEvents) {
                throw Exception("You are already registered.")
            }

            val registrationData = hashMapOf(
                "userId" to uid,
                "eventId" to eventId,
                "checkedIn" to false,
                "registeredAt" to FieldValue.serverTimestamp()
            )

            android.util.Log.e("EVENT_REGISTER", "STEP 6: Creating registration")

            transaction.set(registrationRef, registrationData)

            android.util.Log.e("EVENT_REGISTER", "STEP 7: Updating user")

            transaction.set(
                userRef,
                mapOf(
                    "uid" to uid,
                    "registeredEvents" to (registeredEvents + eventId).distinct()
                ),
                SetOptions.merge()
            )

            val attendeeCount =
                eventSnapshot.getLong("attendeeCount") ?: 0L

            android.util.Log.e("EVENT_REGISTER", "STEP 8: Updating attendee count")

            transaction.update(
                eventRef,
                "attendeeCount",
                attendeeCount + 1
            )

            true

        }.addOnSuccessListener { registered ->

            android.util.Log.e("EVENT_REGISTER", "STEP 9: Transaction completed")

            if (registered) {
                onResult(true, null)
            } else {
                onResult(false, "You are already registered.")
            }

        }.addOnFailureListener { exception ->

            android.util.Log.e(
                "EVENT_REGISTER",
                "TRANSACTION FAILED",
                exception
            )

            onResult(false, exception.message ?: "Registration failed")
        }
    }




    fun unregisterFromEvent(
        eventId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResult(false, "Please sign in first.")
            return
        }

        val eventRef = db.collection("events").document(eventId)
        val userRef = db.collection("users").document(uid)

        db.runTransaction { transaction ->
            val eventSnapshot = transaction.get(eventRef)
            val userSnapshot = transaction.get(userRef)

            if (!eventSnapshot.exists()) {
                throw Exception("Event not found.")
            }

            val registeredEvents =
                userSnapshot.get("registeredEvents") as? List<String>
                    ?: emptyList()

            if (eventId !in registeredEvents) {
                return@runTransaction false
            }

            transaction.update(
                userRef,
                "registeredEvents",
                registeredEvents.filterNot { it == eventId }
            )

            val attendeeCount =
                (eventSnapshot.getLong("attendeeCount") ?: 0L)
                    .coerceAtLeast(0L)

            if (attendeeCount > 0) {
                transaction.update(
                    eventRef,
                    "attendeeCount",
                    FieldValue.increment(-1)
                )
            }

            true
        }.addOnSuccessListener { registered ->
        if (registered) {
            onResult(true, null)
        } else {
            onResult(false, "You are already registered.")
        }
    }.addOnFailureListener { exception ->
        android.util.Log.e(
            "EVENT_REGISTER",
            "Registration failed",
            exception
        )

        exception.printStackTrace()

        onResult(
            false,
            exception.message ?: "Registration failed"
        )
    }
    }

    fun observeRegisteredEvents(
        onResult: (List<String>) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResult(emptyList())
            return
        }

        db.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val registeredEvents =
                    snapshot.get("registeredEvents") as? List<String>
                        ?: emptyList()

                onResult(registeredEvents)
            }
    }
}
