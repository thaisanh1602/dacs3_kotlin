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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.HelpOutline
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.ui.theme.*
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.ScanViewModel
import java.io.File

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

            // 1. Camera preview fullscreen
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

            // Dark overlay on top and bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )

            // 2. Scan frame – rounded rectangle with corner brackets
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.Center)
                    .offset(y = (-30).dp)
            ) {
                // Rounded frame corners
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 4.dp.toPx()
                    val cornerLen = 32.dp.toPx()
                    val radius = 24.dp.toPx()
                    val accent = Color(0xFF52D68A)

                    // Top-left
                    drawLine(accent, Offset(radius, 0f), Offset(cornerLen, 0f), stroke)
                    drawLine(accent, Offset(0f, radius), Offset(0f, cornerLen), stroke)
                    // Top-right
                    drawLine(accent, Offset(size.width - cornerLen, 0f), Offset(size.width - radius, 0f), stroke)
                    drawLine(accent, Offset(size.width, radius), Offset(size.width, cornerLen), stroke)
                    // Bottom-left
                    drawLine(accent, Offset(0f, size.height - cornerLen), Offset(0f, size.height - radius), stroke)
                    drawLine(accent, Offset(radius, size.height), Offset(cornerLen, size.height), stroke)
                    // Bottom-right
                    drawLine(accent, Offset(size.width, size.height - cornerLen), Offset(size.width, size.height - radius), stroke)
                    drawLine(accent, Offset(size.width - cornerLen, size.height), Offset(size.width - radius, size.height), stroke)
                }

                // Animated laser line
                val transition = rememberInfiniteTransition(label = "scanner")
                val laserY by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 260f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "laser_y"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .offset(y = laserY.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF52D68A).copy(alpha = 0.8f),
                                    Color.White.copy(alpha = 0.9f),
                                    Color(0xFF52D68A).copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Instruction label
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 112.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Giữ lá lúa nằm gọn trong khung",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }

            // 3. Close button (top left)
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 48.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 4. Bottom controls panel
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(20.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0D1F0D).copy(alpha = 0.88f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, Color.White.copy(alpha = 0.12f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (scanState is Resource.Loading) {
                        // Analysing state
                        CircularProgressIndicator(
                            color = Color(0xFF52D68A),
                            modifier = Modifier.size(44.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "AI đang phân tích ảnh lá...",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gallery button
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Thư viện",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Thư viện",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }

                            // Main capture button
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .border(3.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                        .padding(5.dp)
                                ) {
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
                                                        Log.e("Upload", "Lỗi Capture Camera", exception)
                                                    }
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF52D68A)
                                        ),
                                        shape = CircleShape,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("📸", fontSize = 24.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Chụp ảnh",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }

                            // Help button
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { /* Open instructions */ },
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = "Hướng dẫn",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Hướng dẫn",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Error display
                        if (scanState is Resource.Error) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "⚠️ ${scanState?.message ?: "Lỗi máy chủ!"}",
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Permission request screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralBg),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(LightMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌾", fontSize = 40.sp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Cần quyền Camera",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "AngriSmart cần Camera để chụp ảnh lá lúa và chẩn đoán sâu bệnh bằng AI.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(27.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Cấp quyền Camera",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
