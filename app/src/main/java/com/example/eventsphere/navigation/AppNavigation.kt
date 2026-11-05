
package com.example.eventsphere.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventsphere.data.CampusEvent
import com.example.eventsphere.screens.admin.AdminDashboardScreen
import com.example.eventsphere.screens.admin.AdminQrCheckInScreen
import com.example.eventsphere.screens.auth.AccessDeniedScreen
import com.example.eventsphere.screens.event.CreateEventScreen
import com.example.eventsphere.screens.event.EventDetailsScreen
import com.example.eventsphere.screens.event.TicketScreen
import com.example.eventsphere.screens.home.HomeScreen
import com.example.eventsphere.screens.myevents.MyEventsScreen
import com.example.eventsphere.screens.profile.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

private object Routes {
    const val HOME = "home"
    const val MY_EVENTS = "my_events"
    const val PROFILE = "profile"
    const val ADMIN = "admin"
    const val CREATE = "create"
    const val DETAILS = "details/{eventId}"
    const val EDIT = "edit/{eventId}"
    const val TICKET = "ticket/{eventId}"
    const val ADMIN_QR_SCANNER = "admin_qr_scanner"

    fun details(id: String) = "details/$id"
    fun edit(id: String) = "edit/$id"
    fun ticket(id: String) = "ticket/$id"
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var user by remember { mutableStateOf(auth.currentUser) }
    var isAdmin by remember { mutableStateOf(false) }
    var roleLoading by remember { mutableStateOf(true) }
    var events by remember { mutableStateOf<List<CampusEvent>>(emptyList()) }
    var registeredIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    DisposableEffect(auth) {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            user = firebaseAuth.currentUser
        }

        auth.addAuthStateListener(authListener)

        onDispose {
            auth.removeAuthStateListener(authListener)
        }
    }

    DisposableEffect(user?.uid) {

        val uid = user?.uid

        var roleListener: ListenerRegistration? = null
        var eventsListener: ListenerRegistration? = null
        var registrationListener: ListenerRegistration? = null

        isAdmin = false
        roleLoading = true
        events = emptyList()
        registeredIds = emptySet()

        if (uid == null) {
            roleLoading = false
        } else {

            roleListener = db.collection("users")
                .document(uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        isAdmin = false
                        roleLoading = false
                        Log.e("EventSphere", "Failed to load role", error)
                    } else {

                        val role = snapshot?.getString("role")
                            ?.trim()
                            ?.lowercase()

                        isAdmin = role == "admin"
                        roleLoading = false

                        Log.d(
                            "EventSphere",
                            "UID=$uid, role=$role, isAdmin=$isAdmin"
                        )
                    }
                }

            eventsListener = db.collection("events")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Log.e("EventSphere", "Failed to load events", error)
                    } else {

                        events = snapshot?.documents?.map { doc ->

                            CampusEvent(
                                id = doc.id,
                                title = doc.getString("title").orEmpty(),
                                description = doc.getString("description").orEmpty(),
                                venue = doc.getString("venue").orEmpty(),
                                category = doc.getString("category") ?: "Other",
                                date = doc.getString("date").orEmpty(),
                                time = doc.getString("time").orEmpty(),
                                attendeeCount =
                                    (doc.getLong("attendeeCount") ?: 0L).toInt(),
                                imageUrl = doc.getString("imageUrl").orEmpty(),
                                ownerId = doc.getString("ownerId").orEmpty()
                            )

                        }.orEmpty()
                    }
                }

            registrationListener = db.collection("users")
                .document(uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Log.e(
                            "EventSphere",
                            "Failed to load registrations",
                            error
                        )
                    } else {

                        registeredIds = snapshot
                            ?.get("registeredEvents")
                            ?.let { value ->
                                (value as? List<*>)
                                    ?.filterIsInstance<String>()
                                    ?.toSet()
                            }
                            ?: emptySet()
                    }
                }
        }

        onDispose {
            roleListener?.remove()
            eventsListener?.remove()
            registrationListener?.remove()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(user?.uid, roleLoading, isAdmin) {

        if (user == null) {
            return@LaunchedEffect
        }

        if (roleLoading) {
            return@LaunchedEffect
        }

        val destination = if (isAdmin) Routes.ADMIN else Routes.HOME

        if (currentRoute == Routes.ADMIN && !isAdmin) {

            navController.navigate(Routes.HOME) {
                popUpTo(Routes.ADMIN) {
                    inclusive = true
                }
                launchSingleTop = true
            }

        } else if (
            currentRoute == null ||
            currentRoute == Routes.HOME ||
            currentRoute == Routes.ADMIN
        ) {

            if (currentRoute != destination) {
                navController.navigate(destination) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    if (user == null || roleLoading) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            CircularProgressIndicator()

            Text(
                text = if (user == null) {
                    "Please sign in to continue"
                } else {
                    "Loading your account..."
                },
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        return
    }

    val mainRoutes = if (isAdmin) {
        setOf(
            Routes.HOME,
            Routes.PROFILE,
            Routes.ADMIN
        )
    } else {
        setOf(
            Routes.HOME,
            Routes.MY_EVENTS,
            Routes.PROFILE
        )
    }

    Scaffold(

        topBar = {

            when (currentRoute) {

                Routes.ADMIN -> {
                    TopAppBar(
                        title = {
                            Text("Admin Dashboard")
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    navController.navigate(Routes.HOME) {
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }

                Routes.CREATE -> {
                    TopAppBar(
                        title = {
                            Text("Create Event")
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    navController.popBackStack()
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }

                Routes.EDIT -> {
                    TopAppBar(
                        title = {
                            Text("Edit Event")
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    navController.popBackStack()
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }

                else -> {
                }
            }
        },

        bottomBar = {

            if (currentRoute in mainRoutes) {

                NavigationBar {

                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = {
                            Text("Home")
                        }
                    )

                    if (!isAdmin) {

                        NavigationBarItem(
                            selected = currentRoute == Routes.MY_EVENTS,
                            onClick = {
                                navController.navigate(Routes.MY_EVENTS) {
                                    launchSingleTop = true
                                }
                            },
                            icon = {
                                Icon(
                                    Icons.Default.EventNote,
                                    contentDescription = "My Events"
                                )
                            },
                            label = {
                                Text("My Events")
                            }
                        )
                    }

                    NavigationBarItem(
                        selected = currentRoute == Routes.PROFILE,
                        onClick = {
                            navController.navigate(Routes.PROFILE) {
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = {
                            Text("Profile")
                        }
                    )

                    if (isAdmin) {

                        NavigationBarItem(
                            selected = currentRoute == Routes.ADMIN,
                            onClick = {
                                navController.navigate(Routes.ADMIN) {
                                    launchSingleTop = true
                                }
                            },
                            icon = {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin"
                                )
                            },
                            label = {
                                Text("Admin")
                            }
                        )
                    }
                }
            }
        }

    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Routes.HOME) {

                HomeScreen(
                    events = events,
                    registeredIds = registeredIds.toList(),
                    isAdmin = isAdmin,

                    onEventClick = {
                        navController.navigate(Routes.details(it.id))
                    },

                    onCreateClick = {
                        if (isAdmin) {
                            navController.navigate(Routes.CREATE)
                        }
                    },

                    onMyEventsClick = {
                        if (!isAdmin) {
                            navController.navigate(Routes.MY_EVENTS)
                        }
                    },

                    onProfileClick = {
                        navController.navigate(Routes.PROFILE)
                    },

                    onAdminClick = {
                        if (isAdmin) {
                            navController.navigate(Routes.ADMIN)
                        }
                    }
                )
            }

            composable(Routes.MY_EVENTS) {

                if (isAdmin) {

                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.ADMIN) {
                            popUpTo(Routes.MY_EVENTS) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }

                } else {

                    MyEventsScreen(
                        events = events.filter {
                            it.id in registeredIds
                        },

                        onEventClick = {
                            navController.navigate(Routes.details(it.id))
                        },

                        onTicketClick = {
                            navController.navigate(Routes.ticket(it.id))
                        },

                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(Routes.PROFILE) {

                ProfileScreen(
                    onBack = {
                        navController.popBackStack()
                    },

                    onLogout = {
                        auth.signOut()
                    }
                )
            }

            composable(Routes.ADMIN) {

                if (isAdmin) {

                    AdminDashboardScreen(
                        events = events,

                        onBack = {
                            navController.navigate(Routes.HOME) {
                                launchSingleTop = true
                            }
                        },

                        onCreateEvent = {
                            navController.navigate(Routes.CREATE)
                        },

                        onEventClick = {
                            navController.navigate(Routes.details(it.id))
                        },

                        onEditEvent = {
                            navController.navigate(Routes.edit(it.id))
                        },

                        onScanQr = {
                            navController.navigate(Routes.ADMIN_QR_SCANNER)
                        },

                        isAdmin = isAdmin
                    )

                } else {
                    AccessDeniedScreen()
                }
            }

            composable(Routes.ADMIN_QR_SCANNER) {

                if (isAdmin) {

                    AdminQrCheckInScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )

                } else {
                    AccessDeniedScreen()
                }
            }

            composable(Routes.CREATE) {

                if (isAdmin) {

                    CreateEventScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )

                } else {
                    AccessDeniedScreen()
                }
            }

            composable(
                route = Routes.EDIT,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->

                val eventId = entry.arguments?.getString("eventId")
                val event = events.find { it.id == eventId }

                if (isAdmin && event != null) {

                    CreateEventScreen(
                        event = event,

                        onSaved = {
                            navController.popBackStack()
                        },

                        onBack = {
                            navController.popBackStack()
                        }
                    )

                } else {
                    AccessDeniedScreen()
                }
            }

            composable(
                route = Routes.DETAILS,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->

                val eventId = entry.arguments?.getString("eventId")
                val event = events.find { it.id == eventId }

                if (event != null) {

                    EventDetailsScreen(
                        event = event,
                        isAdmin = isAdmin,
                        isRegistered = event.id in registeredIds,

                        onBack = {
                            navController.popBackStack()
                        },

                        onRegister = {
                            val uid = auth.currentUser?.uid

                            if (uid != null) {
                                db.collection("users").document(uid)
                                    .update(
                                        "registeredEvents",
                                        com.google.firebase.firestore.FieldValue
                                            .arrayUnion(event.id)
                                    )
                            }
                        },

                        onCancelRegistration = {
                            val uid = auth.currentUser?.uid

                            if (uid != null) {
                                db.collection("users").document(uid)
                                    .update(
                                        "registeredEvents",
                                        com.google.firebase.firestore.FieldValue
                                            .arrayRemove(event.id)
                                    )
                            }
                        },

                        onEdit = {
                            if (isAdmin) {
                                navController.navigate(Routes.edit(event.id))
                            }
                        },

                        onDelete = {
                            if (isAdmin) {
                                db.collection("events").document(event.id)
                                    .delete()

                                navController.popBackStack()
                            }
                        }
                    )

                } else {
                    Text("Event not found")
                }
            }

            composable(
                route = Routes.TICKET,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->

                val eventId = entry.arguments?.getString("eventId")
                val event = events.find { it.id == eventId }

                if (event != null) {

                    TicketScreen(
                        event = event,
                        onBack = {
                            navController.popBackStack()
                        }
                    )

                } else {
                    Text("Ticket not found")
                }
            }
        }
    }
}
