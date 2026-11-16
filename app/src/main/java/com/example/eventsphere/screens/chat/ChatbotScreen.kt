
package com.example.eventsphere.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventsphere.data.ChatbotRepository
import kotlinx.coroutines.launch

data class ChatMessage(
    val message: String,
    val isUser: Boolean
)

@Composable
fun ChatbotScreen() {
    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val repository = remember { ChatbotRepository() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "EventSphere AI Assistant",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { chat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (chat.isUser)
                        Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (chat.isUser)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = chat.message,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (loading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask something...") },
                enabled = !loading,
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                enabled = input.isNotBlank() && !loading,
                onClick = {
                    val question = input.trim()
                    input = ""

                    messages = messages + ChatMessage(question, true)
                    loading = true

                    scope.launch {
                        try {
                            val reply = repository.sendMessage(question)
                            messages = messages + ChatMessage(reply, false)
                        } catch (e: Exception) {
                            messages = messages + ChatMessage(
                                "Error: ${e.localizedMessage ?: "Please try again."}",
                                false
                            )
                        } finally {
                            loading = false
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}

