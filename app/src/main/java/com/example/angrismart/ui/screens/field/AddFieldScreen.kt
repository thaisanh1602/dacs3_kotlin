package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.ui.theme.*
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.FieldViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.compose.ui.text.style.TextAlign

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldScreen(
    viewModel: FieldViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateBackAndRefresh: () -> Unit = {}, // Keep unused exactly
    onSaveSuccess: () -> Unit
) {
    var farmName by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var selectedVariety by remember { mutableStateOf("Bấm để chọn giống lúa...") }
    var expanded by remember { mutableStateOf(false) }
    
    val addState by viewModel.addFarmState.collectAsState()
    
    val varieties = listOf("Lúa ST25", "Jasmine 85", "Đài Thơm 8", "OM5451")
    val scrollState = rememberScrollState() 

    // --- LOCATION GPS LOGIC ---
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationText by remember { mutableStateOf("Bản đồ GPS Google Maps\n(Chưa có tọa độ)") }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            isLoadingLocation = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                    if (lastLoc != null) {
                        isLoadingLocation = false
                        locationText = "Đã lấy tọa độ!\nVĩ độ: ${lastLoc.latitude}\nKinh độ: ${lastLoc.longitude}"
                    } else {
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            CancellationTokenSource().token
                        ).addOnSuccessListener { newLoc: Location? ->
                            isLoadingLocation = false
                            if (newLoc != null) {
                                locationText = "Đã lấy tọa độ!\nVĩ độ: ${newLoc.latitude}\nKinh độ: ${newLoc.longitude}"
                            } else {
                                locationText = "Chưa có tín hiệu GPS.\nHãy mở Google Maps 1 lần hoặc thiết lập tọa độ ảo nếu xài Emulator."
                            }
                        }.addOnFailureListener { e ->
                            isLoadingLocation = false
                            locationText = "Lỗi quét vị trí: ${e.message}"
                        }
                    }
                }.addOnFailureListener {
                    isLoadingLocation = false
                    locationText = "Không thể lấy thông tin vị trí cũ."
                }
            } catch (e: SecurityException) {
                isLoadingLocation = false
                locationText = "Lỗi bảo mật khi lấy định vị."
            }
        } else {
            locationText = "Bạn đã từ chối quyền định vị."
        }
    }

    val requestLocation: () -> Unit = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            isLoadingLocation = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                    if (lastLoc != null) {
                        isLoadingLocation = false
                        locationText = "Đã lấy tọa độ!\nVĩ độ: ${lastLoc.latitude}\nKinh độ: ${lastLoc.longitude}"
                    } else {
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            CancellationTokenSource().token
                        ).addOnSuccessListener { newLoc: Location? ->
                            isLoadingLocation = false
                            if (newLoc != null) {
                                locationText = "Đã lấy tọa độ!\nVĩ độ: ${newLoc.latitude}\nKinh độ: ${newLoc.longitude}"
                            } else {
                                locationText = "Chưa có tín hiệu GPS.\nHãy mở Google Maps 1 lần hoặc thiết lập tọa độ ảo nếu xài Emulator."
                            }
                        }.addOnFailureListener { e ->
                            isLoadingLocation = false
                            locationText = "Lỗi xác định vị trí: ${e.message}"
                        }
                    }
                }.addOnFailureListener {
                    isLoadingLocation = false
                    locationText = "Không thể lấy dữ liệu vị trí cũ."
                }
            } catch (e: SecurityException) {
                isLoadingLocation = false
                locationText = "Lỗi quyền riêng tư định vị."
            }
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    // Success redirect
    LaunchedEffect(addState) {
        if (addState is Resource.Success) {
            viewModel.resetAddFarmState()
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thêm Đồng Ruộng",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = TextPrimary)
                    }
                }
            )
        },
        containerColor = NeutralBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Stylized GPS location map simulator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Background grid decorative borders
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .shadow(2.dp, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Bản đồ",
                            modifier = Modifier.size(32.dp),
                            tint = GreenPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = locationText,
                        color = Color(0xFF37474F),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                
                Button(
                    onClick = requestLocation,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .height(44.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(20.dp),
                    enabled = !isLoadingLocation
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "📍 LẤY VỊ TRÍ NGAY TẠI ĐÂY",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // Input Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .shadow(6.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Thông tin thửa ruộng",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0D3321)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(4.dp)
                            .background(GreenPrimary, RoundedCornerShape(2.dp))
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = farmName,
                        onValueChange = { farmName = it },
                        label = { Text("Tên mảnh ruộng (Ví dụ: Ruộng Ông Bảy)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color(0xFFCFD8DC),
                            focusedLabelColor = GreenPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Diện tích thật (m²)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color(0xFFCFD8DC),
                            focusedLabelColor = GreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rice variety dropdown selector
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedVariety,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Giống lúa đang trồng") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = Color(0xFFCFD8DC),
                                focusedLabelColor = GreenPrimary
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(16.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            varieties.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption, style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        selectedVariety = selectionOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sowing date mock selector
                    OutlinedTextField(
                        value = "29/03/2026", // Preset mockup date value
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ngày gieo sạ (Làm mốc tính toán)") },
                        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Lịch", tint = GreenPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color(0xFFCFD8DC),
                            focusedLabelColor = GreenPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Save error display tag
                    if (addState is Resource.Error) {
                        Text(
                            text = addState?.message ?: "Lỗi cơ sở dữ liệu",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Large Submit Action button with Gradient fill
                    Button(
                        onClick = { viewModel.addFarm(farmName, selectedVariety, area) },
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        enabled = addState !is Resource.Loading
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF0F522E), GreenPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (addState is Resource.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "LƯU CÁNH ĐỒNG NÀY",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
