
package com.example.eventsphere.data

data class CampusEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val venue: String = "",
    val category: String = "Other",
    val date: String = "",
    val time: String = "",
    val attendeeCount: Int = 0,
    val imageUrl: String = "",
    val ownerId: String = ""
)
