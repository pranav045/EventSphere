
package com.example.eventsphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.eventsphere.navigation.AppNavigation
import com.example.eventsphere.screens.auth.AuthScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var signedIn by mutableStateOf(auth.currentUser != null)

            if (signedIn) {
                AppNavigation()
            } else {
                AuthScreen(
                    onAuthSuccess = {
                        signedIn = auth.currentUser != null
                    }
                )
            }
        }
    }
}
