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
import androidx.compose.ui.graphics.Color
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
    
    // Khởi tạo phễu chọn file ảnh từ Bộ sưu tập Điện thoại/Máy Ảo
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Copy ảnh từ Uri (Hệ thống) ra File cục bộ của App để gửi qua mạng
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = File(context.cacheDir, "gallery_leaf.jpg")
                tempFile.outputStream().use { out ->
                    inputStream?.copyTo(out)
                }
                // Khởi động AI phân tích
                viewModel.analyzeImage(tempFile)
            } catch (e: Exception) {
                Log.e("Gallery", "Lỗi mờ ảnh", e)
            }
        }
    }
    
    // Quan sát Flow trạng thái của Retrofit
    val scanState by viewModel.scanState.collectAsState()

    // Theo dõi chuyển đổi để đóng Form khi thành công
    LaunchedEffect(scanState) {
        if (scanState is Resource.Success) {
            val resData = scanState?.data
            viewModel.resetState()
            
            // Sử dụng Mapper để lấy thông tin chi tiết tiếng Việt từ mã bệnh của Server
            val mappedInfo = com.example.angrismart.utils.DiseaseMapper.getInfo(resData?.prediction ?: "")
            
            onNavigateToResult(
                mappedInfo.nameVi,
                "${((resData?.confidence ?: 0.0) * 100).toInt()}%",
                mappedInfo.description,
                mappedInfo.treatment,
                mappedInfo.riskLevel
            )
        }
    }

    // Hộp thoại Sinh Quyền mở Camera từ Android
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
            
            // 1. Phóng luồng Camera Trực Tiếp Lên Màn Hình bằng CameraX
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

            // 2. Ô vuông nhắm lá trị bệnh (Overlay Hướng dẫn Nông Dân)
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.Center)
                    .offset(y = (-40).dp)
                    .border(3.dp, Color(0xFF81C784), RoundedCornerShape(24.dp))
                    .background(Color.Transparent)
            )
            
            Text(
                text = "Vui lòng giữ lá lúa nằm gọn trong khung này",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 120.dp)
                    .background(Color(0xBB000000), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )

            // 3. Nút quay lại (Dấu X)
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .padding(top = 24.dp)
                    .background(Color(0x66000000), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Đóng", tint = Color.White)
            }

            // 4. Panel chụp ảnh đẩy vào AI (Đáy màn hình)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                // Hiển thị vòng xoay đang tải nếu State đang báo Loading
                if (scanState is Resource.Loading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF2E7D32), modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("AI đang tải & phân tích lên Server...", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Nút Mở Thư Viện (Dành cho máy ảo hoặc có ảnh chụp sẵn)
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                modifier = Modifier.size(64.dp),
                                shape = CircleShape
                            ) { 
                                Text("📁", fontSize = 24.sp)
                            }

                            // Nút Chụp Ảnh Camera Trực tiếp
                            Button(
                                onClick = {
                                    val photoFile = File(context.cacheDir, "scan_leaf.jpg")
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                    
                                    // Gói CameraX Lệnh chụp ảnh (Auto)
                                    imageCapture.takePicture(
                                        outputOptions, 
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                // Đẩy hình ảnh chạy thẳng vô Máy chủ Backend Python (Retrofit)
                                                viewModel.analyzeImage(photoFile)
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                Log.e("Upload", "Lỗi rớt Capture Camera")
                                            }
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.size(86.dp), // Nút chụp bự
                                shape = CircleShape
                            ) { 
                                Text("📸", fontSize = 32.sp)
                            }
                        }
                        
                        // Nếu có lỗi từ Server AI (như hỏng CSDL / ngoại tuyến)
                        if (scanState is Resource.Error) {
                            Text(
                                text = scanState?.message ?: "Lỗi máy chủ!",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

    } else {
        // Màn hình Khuyên cấp quyền nếu chưa có
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "AngriSmart cần quyền mở Camera để kiểm tra Lá lúa sâu bệnh.\n(Hoặc tải ảnh lên từ thư viện)",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("CHO PHÉP TRUY CẬP CAMERA")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                Text("HOẶC CHỌN TỪ THƯ VIỆN")
            }
        }
    }

}
