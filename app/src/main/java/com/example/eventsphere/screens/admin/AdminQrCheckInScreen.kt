
package com.example.eventsphere.screens.admin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AdminQrCheckInScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf<Boolean?>(null) }
    var scanSession by remember { mutableIntStateOf(0) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasCameraPermission = granted
        }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun verifyRegistration(qrContent: String) {
        if (isLoading || message == "Check-in successful!") return

        val parts = qrContent.trim().split('|')

        if (parts.size != 2 || parts.any { it.isBlank() }) {
            message = "Invalid QR code format."
            isSuccess = false
            return
        }

        val userId = parts[0].trim()
        val eventId = parts[1].trim()
        val registrationId = "${userId}_$eventId"

        val adminUid = FirebaseAuth.getInstance().currentUser?.uid

        if (adminUid == null) {
            message = "Please sign in first."
            isSuccess = false
            return
        }

        isLoading = true
        message = "Verifying registration..."
        isSuccess = null

        val registrationRef = FirebaseFirestore.getInstance()
            .collection("registrations")
            .document(registrationId)

        FirebaseFirestore.getInstance()
            .runTransaction { transaction ->

                val document = transaction.get(registrationRef)

                if (!document.exists()) {
                    throw Exception("Registration not found.")
                }

                if (document.getString("userId") != userId ||
                    document.getString("eventId") != eventId
                ) {
                    throw Exception("Invalid ticket information.")
                }

                if (document.getBoolean("checkedIn") == true) {
                    throw Exception("This ticket has already been checked in.")
                }

                transaction.update(
                    registrationRef,
                    mapOf(
                        "checkedIn" to true,
                        "checkedInAt" to FieldValue.serverTimestamp(),
                        "checkedInBy" to adminUid
                    )
                )

                "Check-in successful!"
            }
            .addOnSuccessListener { result ->
                isLoading = false
                message = result
                isSuccess = true
            }
            .addOnFailureListener { exception ->
                isLoading = false
                message = exception.localizedMessage
                    ?: "Unable to verify registration."
                isSuccess = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "QR Scanner",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(55.dp)
        )

        Text(
            text = "Event QR Check-In",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Scan a student's event ticket to verify registration.",
            style = MaterialTheme.typography.bodyMedium
        )

        if (hasCameraPermission) {
            key(scanSession) {
                CameraQrPreview(
                    enabled = !isLoading && message.isBlank(),
                    onQrScanned = { qrContent ->
                        verifyRegistration(qrContent)
                    }
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera permission is required.")

                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    ) {
                        Text("Allow Camera")
                    }
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator()
            Text("Verifying ticket...")
        }

        if (message.isNotBlank() && !isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess == true) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSuccess == true) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null
                        )
                    }

                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    message = ""
                    isSuccess = null
                    scanSession++
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Text(" Scan Another Ticket")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CameraQrPreview(
    enabled: Boolean,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        BarcodeScanning.getClient(options)
    }

    val alreadyScanned = remember {
        AtomicBoolean(false)
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        factory = { viewContext ->

            val previewView = PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(viewContext)

            cameraProviderFuture.addListener({

                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

                analysis.setAnalyzer(cameraExecutor) { imageProxy ->

                    if (!enabled || alreadyScanned.get()) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    val mediaImage = imageProxy.image

                    if (mediaImage == null) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->

                            val value = barcodes
                                .firstOrNull {
                                    !it.rawValue.isNullOrBlank()
                                }
                                ?.rawValue

                            if (
                                value != null &&
                                alreadyScanned.compareAndSet(false, true)
                            ) {
                                onQrScanned(value)
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }

                try {
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(viewContext))

            previewView
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            scanner.close()
        }
    }
}
