package com.example.eventsphere.screens.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventsphere.data.CampusEvent
import com.example.eventsphere.data.EventRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    event: CampusEvent? = null,
    onSaved: () -> Unit = {},
    onBack: () -> Unit = {},
    onCancel: () -> Unit = onBack,
    repository: EventRepository = EventRepository
) {
    var title by remember(event?.id) {
        mutableStateOf(event?.title ?: "")
    }

    var description by remember(event?.id) {
        mutableStateOf(event?.description ?: "")
    }

    var category by remember(event?.id) {
        mutableStateOf(event?.category ?: "Technology")
    }

    var date by remember(event?.id) {
        mutableStateOf(event?.date ?: "")
    }

    var time by remember(event?.id) {
        mutableStateOf(event?.time ?: "")
    }

    var venue by remember(event?.id) {
        mutableStateOf(event?.venue ?: "")
    }

    var imageUrl by remember(event?.id) {
        mutableStateOf(event?.imageUrl ?: "")
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf("")
    }

    var saving by remember {
        mutableStateOf(false)
    }

    val categories = listOf(
        "Technology",
        "Cultural",
        "Sports",
        "Workshop",
        "Academic",
        "Other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            category = item
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") },
            placeholder = { Text("2026-11-05") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time") },
            placeholder = { Text("14:00") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = venue,
            onValueChange = { venue = it },
            label = { Text("Venue") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (error.isNotBlank()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            enabled = !saving,
            onClick = {
                if (
                    title.isBlank() ||
                    date.isBlank() ||
                    venue.isBlank()
                ) {
                    error = "Title, date and venue are required."
                    return@Button
                }

                saving = true
                error = ""

                val updatedEvent = CampusEvent(
                    id = event?.id ?: "",
                    title = title.trim(),
                    description = description.trim(),
                    venue = venue.trim(),
                    category = category,
                    date = date.trim(),
                    time = time.trim(),
                    attendeeCount = event?.attendeeCount ?: 0,
                    imageUrl = imageUrl.trim(),
                    ownerId = event?.ownerId ?: ""
                )

                repository.saveEvent(updatedEvent) { success, message ->
                    saving = false

                    if (success) {
                        onSaved()
                    } else {
                        error = message ?: "Unable to save event."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saving) "Saving..." else "Save Event")
        }

        OutlinedButton(
            onClick = onCancel,
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}