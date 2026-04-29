package com.example.angrismart.ui.screens.scan

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.ScanViewModel
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDiseaseScreen(
    viewModel: ScanViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToResult: (String, String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = File(context.cacheDir, "gallery_leaf.jpg")
                tempFile.outputStream().use { out ->
                    inputStream?.copyTo(out)
                }
                viewModel.analyzeImage(tempFile)
            } catch (e: Exception) {
                Log.e("Gallery", "Lỗi mở ảnh", e)
            }
        }
    }

    val scanState by viewModel.scanState.collectAsState()

    LaunchedEffect(scanState) {
        if (scanState is Resource.Success) {
            val resData = scanState?.data
            viewModel.resetState()
            
            if (resData != null) {
                onNavigateToResult(
                    resData.diseaseName,
                    resData.confidence,
                    resData.description,
                    resData.treatment,
                    resData.riskLevel
                )
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // 1. Camera View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraX", "Binding failed", e)
                    }
                    previewView
                }
            )

            // 2. Animated Scanner Box
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.Center)
                    .offset(y = (-40).dp)
            ) {
                // Border frame
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(3.dp, Color(0xFF00E676).copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    // Laser Animation
                    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
                    val position by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 280f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "laser_position"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .offset(y = position.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF00E676),
                                        Color.Transparent
                                    )
                                )
                            )
                            .shadow(8.dp, spotColor = Color(0xFF00E676))
                    )
                }
            }

            // Instructional text with shadow
            Text(
                text = "Giữ lá lúa nằm gọn trong khung",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 8f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 130.dp)
                    .background(Color(0x66000000), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // 3. Top Action (Close)
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .padding(top = 24.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Đóng", tint = Color.White)
            }

            // 4. Bottom Glassmorphism Panel
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (scanState is Resource.Loading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFF00E676),
                            modifier = Modifier.size(50.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AI đang xử lý hình ảnh...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nút Thư viện
                        IconButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Text("📁", fontSize = 24.sp)
                        }

                        // Nút Chụp Ảnh bự
                        Button(
                            onClick = {
                                val photoFile = File(context.cacheDir, "scan_leaf.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                
                                imageCapture.takePicture(
                                    outputOptions, 
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            viewModel.analyzeImage(photoFile)
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Log.e("Upload", "Lỗi Capture Camera")
                                        }
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(80.dp)
                                .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Text("📸", fontSize = 28.sp)
                        }

                        // Fake button for spacing balance (or can be flash toggle later)
                        Box(modifier = Modifier.size(56.dp))
                    }

                    if (scanState is Resource.Error) {
                        Text(
                            text = scanState?.message ?: "Lỗi máy chủ!",
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-30).dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🌾",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AngriSmart cần sử dụng Camera để chẩn đoán sâu bệnh trên lúa.",
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("CẤP QUYỀN CAMERA", fontWeight = FontWeight.Bold)
            }
        }
    }
}
