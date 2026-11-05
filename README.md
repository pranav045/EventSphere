# EventSphere 🎓📅

**EventSphere** is a campus event management and discovery Android application developed using **Kotlin, Jetpack Compose, and Firebase**.

The application helps students discover campus events, register for events, view their registered events, and manage their profiles. Administrators can create, edit, delete, and manage campus events.

---

## 📱 Features

### 👨‍🎓 Student Features

- 🔐 Student registration and login
- 👤 Student profile management
- 🔎 Search campus events
- 🏷️ Filter events by category
- 📅 View event details
- 📝 Register for events
- 🎟️ View registered events
- 📊 Track event attendance
- 🌙 Dark mode support
- 🔔 Event notification support

### 👨‍💼 Admin Features

- 🔐 Admin authentication
- ➕ Create new events
- ✏️ Edit existing events
- 🗑️ Delete events
- 📊 Admin dashboard
- 👥 View event registrations
- 📱 QR-based event check-in
- 📈 Monitor event participation

---

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Application development |
| Jetpack Compose | Modern Android UI |
| Firebase Authentication | User authentication |
| Firebase Firestore | Cloud database |
| Firebase Cloud Messaging | Notifications |
| Material 3 | UI components |
| Android Studio | Development environment |
| Gradle | Build system |

---

## 🏗️ Project Structure

```text
EventSphere/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/eventsphere/
│           │       │
│           │       ├── data/
│           │       │   ├── CampusEvent.kt
│           │       │   └── EventRepository.kt
│           │       │
│           │       ├── navigation/
│           │       │   └── AppNavigation.kt
│           │       │
│           │       ├── screens/
│           │       │   ├── auth/
│           │       │   │   └── AuthScreen.kt
│           │       │   │
│           │       │   ├── home/
│           │       │   │   └── HomeScreen.kt
│           │       │   │
│           │       │   ├── event/
│           │       │   │   ├── CreateEventScreen.kt
│           │       │   │   └── EventDetailsScreen.kt
│           │       │   │
│           │       │   ├── admin/
│           │       │   │   ├── AdminDashboardScreen.kt
│           │       │   │   ├── AdminScreen.kt
│           │       │   │   └── AdminQrCheckInScreen.kt
│           │       │   │
│           │       │   └── profile/
│           │       │       └── ProfileScreen.kt
│           │       │
│           │       └── MainActivity.kt
│           │
│           └── res/
│               ├── drawable/
│               │   └── eventsphere_logo.png
│               ├── mipmap/
│               └── values/
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔥 Firebase Setup

EventSphere uses Firebase for authentication and cloud data storage.

### Firebase Services

The project uses:

- Firebase Authentication
- Cloud Firestore
- Firebase Cloud Messaging

### Authentication

Firebase Authentication is used for student and administrator login.

Users are stored in the Firestore `users` collection.

Example user document:

```text
users
 └── userId
      ├── uid
      ├── name
      ├── email
      ├── college
      └── role
```

The `role` field is used to identify whether the user is a:

```text
student
```

or

```text
admin
```

---

## 📅 Event Data

Events are stored in Cloud Firestore.

Example event structure:

```text
events
 └── eventId
      ├── title
      ├── description
      ├── category
      ├── venue
      ├── date
      ├── time
      ├── imageUrl
      ├── attendeeCount
      └── ownerId
```

---


## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/pranav045/EventSphere.git
```

### 2. Open the project

Open the project using **Android Studio**.

### 3. Configure Firebase

Create a Firebase project and add your Android application.

Use the package name:

```text
com.example.eventsphere
```

Configure Firebase Authentication and Cloud Firestore.

### 4. Add Firebase configuration

Place your Firebase configuration file in:

```text
app/google-services.json
```

Do not commit this file if your repository policy requires keeping it private.

### 5. Build the project

Sync Gradle and run:

```text
Build → Make Project
```

### 6. Run the application

Connect an Android device or start an Android Emulator and click:

```text
Run ▶
```

---

## 🎨 Application UI

The application follows **Material 3** design principles and uses Jetpack Compose for the user interface.

Main screens include:

```text
Login / Register
       ↓
     Home
       ↓
Event Details
       ↓
Event Registration
       ↓
My Events
       ↓
Profile
```

Administrators have access to:

```text
Admin Dashboard
       ↓
Create Event
       ↓
Edit Event
       ↓
Manage Events
       ↓
QR Check-in
```

---

## 📸 Screenshots

Add screenshots of your application here.

Example:

```markdown
## Screenshots

### Login
![Login Screen](screenshots/login.png)

### Home
![Home Screen](screenshots/home.png)

### Event Details
![Event Details](screenshots/event-details.png)

### Admin Dashboard
![Admin Dashboard](screenshots/admin-dashboard.png)
```

Recommended screenshot folder:

```text
screenshots/
├── login.png
├── home.png
├── event-details.png
├── my-events.png
├── profile.png
└── admin-dashboard.png
```

---

## 🎯 Future Improvements

Possible future enhancements include:

- Google Sign-In
- Advanced event recommendations
- AI-based event recommendations
- Event ratings and reviews
- Calendar integration
- Improved QR attendance tracking
- Push notification scheduling
- Event analytics
- Online event support
- College-wise event discovery

---

## 👨‍💻 Developer

**Pranav Gaira**

B.Tech Computer Science & Engineering

GitHub: [github.com/pranav045](https://github.com/pranav045)

LinkedIn: [linkedin.com/in/pranav-gaira](https://linkedin.com/in/pranav-gaira)

---

## 📄 License

This project was developed as an academic and portfolio project.

You may modify and extend the project for educational purposes.

---

## ⭐ Project Goal

The goal of **EventSphere** is to provide a centralized digital platform where students can easily **discover, register for, and participate in campus events**, while administrators can efficiently **create and manage events and monitor participation**.

**EventSphere — Discover. Connect. Participate. 🎓**
