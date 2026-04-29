package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.domain.model.Farm
import com.example.angrismart.ui.theme.GreenPrimary
import com.example.angrismart.ui.theme.GreenSecondary
import com.example.angrismart.ui.theme.YellowWarning
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.FieldViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldDetailScreen(
    fieldId: String,
    viewModel: FieldViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {}
) {
    val farmsState by viewModel.farmsState.collectAsState()
    val farm = (farmsState.data ?: emptyList()).find { it.id == fieldId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(farm?.farmName ?: "Chi tiết ruộng", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Sửa */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (farm == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                if (farmsState is Resource.Loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Không tìm thấy dữ liệu ruộng")
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Thẻ Thông tin chính
            InfoCard(farm)

            // Thẻ Vị trí Bản Đồ (Google Maps View)
            val lat = farm.latitude
            val lng = farm.longitude
            if (lat != null && lng != null) {
                Spacer(modifier = Modifier.height(24.dp))
                FieldMapLocation(lat, lng, farm.farmName)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Thẻ Tiến độ mùa vụ
            GrowthProgressCard(farm)

            Spacer(modifier = Modifier.height(24.dp))

            // Thẻ Hành động chính
            MainActions(onNavigateToScan, onNavigateToAddTransaction)

            Spacer(modifier = Modifier.height(24.dp))

            // Lịch sử & Báo cáo (Placeholders)
            HistorySection()
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoCard(farm: Farm) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = GreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Thông tin thửa ruộng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

            DetailItem("Giống lúa", farm.varietyName, "🌱")
            DetailItem("Diện tích", "${farm.areaM2} m²", "📐")
            DetailItem("Tình trạng", if(farm.status == "active") "Đang canh tác" else "Đã thu hoạch", "✅")
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, icon: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
        }
        Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun GrowthProgressCard(farm: Farm) {
    val progress = (farm.ageDays.toFloat() / farm.totalGrowthDays.toFloat()).coerceIn(0f, 1f)
    val stageName = getStageName(farm.ageDays, farm.totalGrowthDays)
    val stageColor = getStageColor(farm.ageDays, farm.totalGrowthDays)
    
    val stages = listOf("Mạ non", "Đẻ nhánh", "Làm đòng", "Chín", "Thu hoạch")
    val currentStageIndex = when {
        progress < 0.2f -> 0
        progress < 0.5f -> 1
        progress < 0.8f -> 2
        progress < 1.0f -> 3
        else -> 4
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dòng thời gian lúa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = stageColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Ngày thứ ${farm.ageDays}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = stageColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // -- Biểu Đồ Thanh Ngang (Timeline Stepper) --
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Đường line ngang dính liền (nằm dưới)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until stages.size - 1) {
                            val isLineActive = i < currentStageIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .background(if (isLineActive) GreenPrimary else Color(0xFFEEEEEE))
                            )
                        }
                    }

                    // Các điểm mốc (Nodes nằm trên line ngang)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        stages.forEachIndexed { index, _ ->
                            val isActive = index <= currentStageIndex
                            val isCurrent = index == currentStageIndex
                            
                            Box(
                                modifier = Modifier
                                    .size(if (isCurrent) 22.dp else 16.dp)
                                    .background(if (isActive) GreenPrimary else Color(0xFFEEEEEE), androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                // Lõi trắng nhỏ bé bên trong mốc hiện tại
                                if (isCurrent) {
                                    Box(modifier = Modifier.size(10.dp).background(Color.White, androidx.compose.foundation.shape.CircleShape))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Tên các mốc
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    stages.forEachIndexed { index, name ->
                        val isCurrent = index == currentStageIndex
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            color = if (isCurrent) GreenPrimary else Color.Gray,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.width(55.dp) // Cố định nhẹ để chữ nằm đúng vị trí chấm tròn
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainActions(onNavigateToScan: () -> Unit, onNavigateToAddTransaction: () -> Unit) {
    Column {
        Text(
            text = "Hành động nhanh",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Button(
            onClick = onNavigateToScan,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            Text("🔍 QUÉT SÂU BỆNH CHO RUỘNG NÀY", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onNavigateToAddTransaction,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("💰 THÊM KHOẢN THU / CHI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HistorySection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lịch sử chẩn đoán",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* TODO */ }) {
                Text("Xem tất cả", color = GreenPrimary)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Chưa có lịch sử quét nào cho ruộng này", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun FieldMapLocation(lat: Double, lng: Double, name: String) {
    Column {
        Text(
            text = "Vị trí địa lý",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            val position = LatLng(lat, lng)
            val cameraState = rememberCameraPositionState {
                this.position = CameraPosition.fromLatLngZoom(position, 16f)
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false, tiltGesturesEnabled = false, rotationGesturesEnabled = false)
            ) {
                Marker(
                    state = MarkerState(position = position),
                    title = name,
                    snippet = "Tọa độ: $lat, $lng"
                )
            }
        }
    }
}
