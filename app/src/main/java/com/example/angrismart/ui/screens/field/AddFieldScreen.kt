package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldScreen(
    viewModel: FieldViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateBackAndRefresh: () -> Unit = {},
    onSaveSuccess: () -> Unit
) {
    var farmName by remember { mutableStateOf(TextFieldValue("")) }
    var area by remember { mutableStateOf(TextFieldValue("")) }
    var selectedVariety by remember { mutableStateOf("Bấm để chọn giống lúa...") }
    var expanded by remember { mutableStateOf(false) }
    
    val addState by viewModel.addFarmState.collectAsState()
    val varieties = listOf("Lúa ST25", "Jasmine 85", "Đài Thơm 8", "OM5451")
    val scrollState = rememberScrollState() 

    val context = LocalContext.current
    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }
    
    val mapViewModel: com.example.angrismart.ui.screens.map.MapViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.example.angrismart.ui.screens.map.MapViewModel(com.example.angrismart.data.remote.MapRetrofitClient.mapService) as T
            }
        }
    )
    val farmlands by mapViewModel.farmlands.collectAsState()
    val mapCenter by mapViewModel.mapCenter.collectAsState()
    val selectedFarmland by mapViewModel.selectedFarmland.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(LatLng(10.762622, 106.660172), 5f)
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
            val zoom = cameraPositionState.position.zoom
            mapViewModel.onMapBoundsChanged(bounds, zoom)
        }
    }

    LaunchedEffect(mapCenter) {
        mapCenter?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    // --- LOGIC ĐỊNH VỊ GPS ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if ((permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) || (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)) {
            isLoadingLocation = true
            fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { lastLoc: Location? ->
                isLoadingLocation = false
                if (lastLoc != null) {
                    val latlng = LatLng(lastLoc.latitude, lastLoc.longitude)
                    currentLatLng = latlng
                    mapViewModel.searchLocation("${lastLoc.latitude},${lastLoc.longitude}") // Move camera manually
                }
            }
        }
    }

    val requestLocation: () -> Unit = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine) {
            isLoadingLocation = true
            fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { lastLoc: Location? ->
                isLoadingLocation = false
                if (lastLoc != null) {
                    val latlng = LatLng(lastLoc.latitude, lastLoc.longitude)
                    currentLatLng = latlng
                    cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(latlng, 16f)
                }
            }
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    LaunchedEffect(addState) {
        if (addState is Resource.Success) {
            viewModel.resetAddFarmState()
            onSaveSuccess()
        }
    }
    
    LaunchedEffect(selectedFarmland) {
        selectedFarmland?.tags?.get("area")?.let { areaValue ->
            area = TextFieldValue(areaValue)
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
                // ĐÃ XÓA verticalScroll TẠI ĐÂY ĐỂ TRÁNH XUNG ĐỘT SCROLL MAP
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tìm xã, huyện, ấp...") },
                    shape = RoundedCornerShape(12.dp)
                )
                IconButton(onClick = { mapViewModel.searchLocation(searchQuery) }) {
                    Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = GreenPrimary)
                }
            }
        
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                com.google.maps.android.compose.GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = com.google.maps.android.compose.MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                    onMapClick = { latLng ->
                        currentLatLng = latLng
                    }
                ) {
                    // Manual pin
                    currentLatLng?.let {
                        com.google.maps.android.compose.Marker(
                            state = com.google.maps.android.compose.MarkerState(position = it),
                            title = "Vị trí được chọn"
                        )
                    }

                    // Farmland polygons
                    farmlands.forEach { farmland ->
                        com.google.maps.android.compose.Polygon(
                            points = farmland.points,
                            fillColor = if (selectedFarmland?.id == farmland.id)
                                Color(0x64FF0000)
                            else
                                Color(0x6400FF00),
                            strokeColor = Color.DarkGray,
                            strokeWidth = 3f,
                            clickable = true,
                            onClick = {
                                mapViewModel.selectFarmland(farmland)
                                currentLatLng = farmland.points.first()
                            }
                        )
                    }
                }
                
                Button(
                    onClick = requestLocation,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = !isLoadingLocation
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("📍", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    label = { Text("Tên mảnh ruộng (VD: Ruộng Ông Bảy)") },
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Diện tích thật (m²)") },
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = GreenPrimary),
                        modifier = Modifier.menuAnchor().fillMaxWidth().height(68.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        varieties.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedVariety = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = "29/03/2026",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ngày gieo sạ") },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Lịch", tint = GreenPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary),
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (addState is Resource.Error) {
                    Text(
                        text = addState?.message ?: "Lỗi csdl",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    onClick = { viewModel.addFarm(farmName.text, selectedVariety, area.text, currentLatLng?.latitude, currentLatLng?.longitude) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
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
