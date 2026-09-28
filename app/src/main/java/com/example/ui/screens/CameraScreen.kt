package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PinggoViewModel
import com.google.accompanist.permissions.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: PinggoViewModel,
    onBack: () -> Unit,
    onShared: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            cameraPermissionState.status.isGranted -> {
                RealCameraContent(viewModel, onBack, onShared)
            }
            cameraPermissionState.status.shouldShowRationale -> {
                PermissionDeniedContent("Camera permission is needed to take photos.", onBack) {
                    cameraPermissionState.launchPermissionRequest()
                }
            }
            else -> {
                PermissionDeniedContent("Camera permission denied. Please enable it in Settings.", onBack) {
                    // Open settings could be added here
                }
            }
        }
    }
}

@Composable
fun RealCameraContent(
    viewModel: PinggoViewModel,
    onBack: () -> Unit,
    onShared: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var showShareSheet by remember { mutableStateOf(false) }
    
    val preview = remember { Preview.Builder().build() }
    val imageCapture = remember { ImageCapture.Builder().setFlashMode(flashMode).build() }
    val cameraSelector = remember(lensFacing) { CameraSelector.Builder().requireLensFacing(lensFacing).build() }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(lensFacing, flashMode) {
        imageCapture.flashMode = flashMode
        val cameraProvider = context.getCameraProvider()
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        } catch (e: Exception) {
            Log.e("CameraScreen", "Use case binding failed", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (capturedUri == null) {
            // Live Preview
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // Overlays
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    
                    IconButton(
                        onClick = {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                                else -> ImageCapture.FLASH_MODE_OFF
                            }
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (flashMode == ImageCapture.FLASH_MODE_ON) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (flashMode == ImageCapture.FLASH_MODE_ON) Color.Yellow else Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Switch Camera
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier.size(54.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.FlipCameraAndroid, "Switch Camera", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    
                    // Capture Button
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                capturePhoto(context, imageCapture) { uri ->
                                    capturedUri = uri
                                }
                            }
                    )
                    
                    // Gallery Placeholder or Empty Spacer for balance
                    Spacer(modifier = Modifier.size(54.dp))
                }
            }
        } else {
            // Photo Preview
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = capturedUri,
                    contentDescription = "Captured photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        IconButton(
                            onClick = { capturedUri = null },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, "Discard", tint = Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GlassButton(
                            text = "Retake",
                            isPrimary = false,
                            onClick = { capturedUri = null },
                            modifier = Modifier.weight(1f)
                        )
                        GlassButton(
                            text = "Use Photo",
                            isPrimary = true,
                            onClick = { showShareSheet = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Share Selection sheet
        if (showShareSheet && capturedUri != null) {
            PhotoShareSheet(
                viewModel = viewModel,
                photoUri = capturedUri!!,
                onDismiss = { showShareSheet = false },
                onShared = {
                    showShareSheet = false
                    onShared()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoShareSheet(
    viewModel: PinggoViewModel,
    photoUri: Uri,
    onDismiss: () -> Unit,
    onShared: () -> Unit
) {
    val conversations by viewModel.conversations.collectAsState()
    val currentUser by viewModel.userProfile.collectAsState()
    val myUid = currentUser?.uid ?: ""
    
    var isUploading by remember { mutableStateOf(false) }
    var selectedConvId by remember { mutableStateOf<String?>(null) }
    var shareMode by remember { mutableStateOf("none") } // "chat", "status"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White.copy(alpha = 0.95f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Share Photo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = LightPrimaryText,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            if (isUploading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PinggoPinkPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Uploading your magic... ✨", color = LightSecondaryText)
                    }
                }
            } else {
                // Options
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Status Option
                    ShareOptionItem(
                        title = "Add to My Status",
                        icon = Icons.Default.AddAPhoto,
                        isSelected = shareMode == "status",
                        onClick = { shareMode = "status"; selectedConvId = null }
                    )
                    
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    
                    Text(
                        text = "Send in Chat",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = LightSecondaryText,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Box(modifier = Modifier.heightIn(max = 300.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(conversations) { conv ->
                                val title = conv.getTitle(myUid)
                                val avatar = conv.getAvatarUrl(myUid)
                                val isSelected = selectedConvId == conv.id
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) PinggoPinkLight.copy(alpha = 0.2f) else Color.Transparent)
                                        .clickable { 
                                            selectedConvId = conv.id
                                            shareMode = "chat"
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GlassAvatar(photoUrl = avatar, name = title, size = 44.dp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PinggoPinkPrimary else LightPrimaryText,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, null, tint = PinggoPinkPrimary, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    GlassButton(
                        text = when(shareMode) {
                            "chat" -> "Send to Chat"
                            "status" -> "Post to Status"
                            else -> "Select an Option"
                        },
                        enabled = shareMode != "none",
                        onClick = {
                            isUploading = true
                            if (shareMode == "status") {
                                viewModel.uploadStatusMedia(photoUri, false, "", { /* progress */ }, { success ->
                                    if (success) onShared()
                                    else isUploading = false
                                })
                            } else if (shareMode == "chat" && selectedConvId != null) {
                                // We need a way to send to a specific conversation
                                // I'll assume we can use a temporary workaround or update ViewModel
                                viewModel.sendImageToSpecificConversation(photoUri, selectedConvId!!, onComplete = {
                                    onShared()
                                })
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ShareOptionItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PinggoPinkLight.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(PinggoPinkLight.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = PinggoPinkPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) PinggoPinkPrimary else LightPrimaryText
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, null, tint = PinggoPinkPrimary)
        }
    }
}

@Composable
fun PermissionDeniedContent(message: String, onBack: () -> Unit, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CameraAlt, null, tint = Color.Gray, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                Text("Go Back")
            }
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PinggoPinkPrimary)) {
                Text("Try Again")
            }
        }
    }
}

private fun capturePhoto(context: Context, imageCapture: ImageCapture, onCaptured: (Uri) -> Unit) {
    val photoFile = File(
        context.cacheDir,
        "PINGGO_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
    )
    
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onCaptured(Uri.fromFile(photoFile))
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Capture failed", exception)
            }
        }
    )
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProviderFuture ->
        cameraProviderFuture.addListener({
            continuation.resume(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }
}
