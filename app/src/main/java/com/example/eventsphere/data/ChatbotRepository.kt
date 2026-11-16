
package com.example.eventsphere.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ChatbotRepository {

    private val workerUrl =
        "https://eventsphere-chatbot.pranavgaira.workers.dev/"

    suspend fun sendMessage(message: String): String {

        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Please login to use the chatbot")

        val token = user.getIdToken(false).await().token
            ?: throw Exception("Unable to verify your login")

        return withContext(Dispatchers.IO) {

            val connection =
                URL(workerUrl).openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 60000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $token"
                )

                val requestBody = JSONObject()
                    .put("message", message)
                    .toString()

                connection.outputStream.use {
                    it.write(requestBody.toByteArray(Charsets.UTF_8))
                }

                val statusCode = connection.responseCode

                val responseStream =
                    if (statusCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val responseText =
                    responseStream.bufferedReader().use { it.readText() }

                val responseJson = JSONObject(responseText)

                if (statusCode !in 200..299) {
                    throw Exception(
                        responseJson.optString(
                            "error",
                            "Chatbot request failed"
                        )
                    )
                }

                responseJson.getString("reply")

            } finally {
                connection.disconnect()
            }
        }
    }
}
