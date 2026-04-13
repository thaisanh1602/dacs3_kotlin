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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.ui.theme.GreenPrimary
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
    onNavigateBackAndRefresh: () -> Unit = {}, // Use this when success
    onSaveSuccess: () -> Unit
) {
    var farmName by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var selectedVariety by remember { mutableStateOf("Bấm để chọn giống lúa...") }
    var expanded by remember { mutableStateOf(false) }
    
    val addState by viewModel.addFarmState.collectAsState()
    
    val varieties = listOf("Lúa ST25", "Jasmine 85", "Đài Thơm 8", "OM5451")
    val scrollState = rememberScrollState() 

    // --- LOGIC ĐỊNH VỊ GPS ---
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
                        // Nếu chưa có vị trí lưu trong cache, ép quét GPS mới
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

    // Quan sát nếu ghi thành công lên Đám Mây thì chuyển hướng về
    LaunchedEffect(addState) {
        if (addState is Resource.Success) {
            viewModel.resetAddFarmState()
            onSaveSuccess()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm Đồng Ruộng", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Giả lập Bản đồ lấy GPS siêu dễ dàng
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Bản đồ", modifier = Modifier.size(48.dp), tint = GreenPrimary)
                    Text(locationText, color = Color.DarkGray, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
                
                Button(
                    onClick = requestLocation,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = !isLoadingLocation
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("📍 LẤY VỊ TRÍ NGAY TẠI ĐÂY", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Dữ liệu Nhập liệu (Form)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Thông tin thửa ruộng",
                    style = MaterialTheme.typography.titleLarge,
                    color = GreenPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = farmName,
                    onValueChange = { farmName = it },
                    label = { Text("Tên mảnh ruộng (VD: Ruộng Ông Bảy)", style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Diện tích thật (m²)", style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown Giống lúa (Rất to, cực kì dễ bấm bằng tay không)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedVariety,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Giống lúa đang trồng", style = MaterialTheme.typography.bodyLarge) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = GreenPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .height(68.dp),
                        shape = RoundedCornerShape(12.dp)
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

                // DatePicker (Ngày gieo sạ để bộ đếm Firebase tính toán cho chính xác)
                OutlinedTextField(
                    value = "29/03/2026", // Giả lập DatePicker
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ngày gieo sạ (Làm mốc tính toán)", style = MaterialTheme.typography.bodyLarge) },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Lịch", tint = GreenPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Báo Lỗi Chữ Đỏ nếu Save hụt
                if (addState is Resource.Error) {
                    Text(
                        text = addState?.message ?: "Lỗi csdl",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Nút bấm khổng lồ Lưu Dữ Liệu
                Button(
                    onClick = { viewModel.addFarm(farmName, selectedVariety, area) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = addState !is Resource.Loading
                ) {
                    if (addState is Resource.Loading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("LƯU CÁNH ĐỒNG NÀY", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

            }
        }
    }
}
