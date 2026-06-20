package com.example.angrismart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale
import java.util.Calendar
import com.example.angrismart.ui.theme.*
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.WeatherViewModel
import com.example.angrismart.viewmodel.FieldViewModel
import com.example.angrismart.domain.model.Farm
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import android.annotation.SuppressLint
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

// Deterministic farm health calculator based on ID to make the experience real
fun getFarmHealth(farm: Farm): Int {
    val seed = farm.id.hashCode()
    return 55 + (kotlin.math.abs(seed) % 41) // Health values range from 55 to 95
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    weatherViewModel: WeatherViewModel = viewModel(),
    fieldViewModel: FieldViewModel = viewModel(),
    userName: String = "Bà con",
    onNavigateToFields: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToWeather: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfit: () -> Unit = {},
    onNavigateToFieldDetail: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val weatherState by weatherViewModel.currentWeather.collectAsState()
    val diseaseRisk by weatherViewModel.diseaseRisk.collectAsState()
    val farmsState by fieldViewModel.farmsState.collectAsState()
    val riceVariantsState by fieldViewModel.riceVariantsState.collectAsState()

    val rawFarmsAll = farmsState.data ?: emptyList()
    val rawFarms = remember(rawFarmsAll) { rawFarmsAll.filter { it.isHarvested == 0 && it.status == "active" } }
    val variants = riceVariantsState.data ?: emptyList()

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarseGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        if (fineGranted || coarseGranted) {
            @SuppressLint("MissingPermission")
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        weatherViewModel.loadWeather(location.latitude, location.longitude)
                    } else {
                        weatherViewModel.loadWeather()
                    }
                }
        } else {
            weatherViewModel.loadWeather()
        }
    }

    LaunchedEffect(Unit) {
        fieldViewModel.loadFarms()
        fieldViewModel.loadRiceVariants()

        // Location check
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            @SuppressLint("MissingPermission")
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        weatherViewModel.loadWeather(location.latitude, location.longitude)
                    } else {
                        weatherViewModel.loadWeather()
                    }
                }
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Greeting logic based on time
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingText = when {
        hour < 12 -> "Chào buổi sáng,"
        hour < 18 -> "Chào buổi chiều,"
        else -> "Chào buổi tối,"
    }

    // Farms count needing attention (health < 60%)
    val needyFarmsCount = rawFarms.count { getFarmHealth(it) < 60 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GlassBgStart, GlassBgEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. GREETING & HEALTH STATE PILL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$greetingText !",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Glassmorphic status capsule
                    Box(
                        modifier = Modifier
                            .background(GlassCardBg, RoundedCornerShape(20.dp))
                            .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (needyFarmsCount > 0) WarningAmber else MintGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (needyFarmsCount > 0) "$needyFarmsCount ruộng lúa cần chăm sóc" else "Tất cả ruộng đều khỏe mạnh",
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                // Small round decorative avatar / leaf badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(GlassCardBg)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(26.dp))
                        .clickable { showLogoutDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌾", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Banner cảnh báo dịch hại (nếu có từ AI/Weather)
            diseaseRisk?.let { risk ->
                if (risk.hasRisk) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clickable { onNavigateToWeather() },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.horizontalGradient(listOf(Color(0xFFFFEBEE), Color.White)))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Cảnh báo",
                                tint = DangerRed,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Cảnh báo bệnh: ${risk.diseaseName}\nDự kiến: ${risk.riskDay}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = DangerRed,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // 2. QUICK DIAGNOSIS CARD (Chẩn đoán nhanh)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFE8F5E9).copy(alpha = 0.9f), Color(0xFFC8E6C9).copy(alpha = 0.7f))
                            )
                        )
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Chẩn đoán nhanh",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chụp ảnh lá lúa và nhận phân tích AI tức thì trong vài giây.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text("⏱️ Kết quả tức thì", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = LightMint.copy(alpha = 0.6f))
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("✨ Công nghệ AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = LightMint.copy(alpha = 0.6f))
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = onNavigateToScan,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📸 Bắt đầu quét lá", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. WEATHER STATUS SUMMARY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassCardBg)
                    .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
                    .clickable { onNavigateToWeather() }
                    .padding(16.dp)
            ) {
                WeatherCardContent(weatherState)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. MY GARDEN / MY FIELDS SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cánh đồng của tôi",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(
                        onClick = { onNavigateToFields() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(GlassCardBg)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm ruộng", tint = ForestGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onNavigateToFields,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(GlassCardBg)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Xem tất cả", tint = ForestGreen, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fields list inside dashboard
            if (farmsState is Resource.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (rawFarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassCardBg)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có cánh đồng nào. Hãy bấm nút + để tạo mới cánh đồng.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Show top 4 fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rawFarms.take(4).forEach { farm ->
                        val variantName = variants.find { it.id == farm.varietyId }?.name ?: farm.varietyId
                        DashboardFarmItem(
                            farm = farm,
                            variantName = variantName,
                            onClick = { onNavigateToFieldDetail(farm.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. OTHER QUICK ACCESSIBLE CARD ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToProfit() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📊 Lợi nhuận", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Theo dõi doanh thu & chi phí", color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToChat() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🤖 Hỏi đáp AI", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Hỏi đáp chuyên sâu với chuyên gia", color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Đăng xuất", fontWeight = FontWeight.Bold) },
                text = { Text("Bà con có chắc chắn muốn đăng xuất khỏi tài khoản hiện tại không?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }
                    ) {
                        Text("Đăng xuất", color = DangerRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Hủy", color = TextPrimary)
                    }
                }
            )
        }


    }
}

@Composable
fun WeatherCardContent(weatherState: Resource<com.example.angrismart.data.remote.model.CurrentWeather>) {
    when (weatherState) {
        is Resource.Loading -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ForestGreen)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Đang tải thời tiết...", color = TextSecondary)
            }
        }
        is Resource.Success -> {
            val weather = weatherState.data!!
            val (emoji, desc) = WeatherViewModel.weatherDescription(weather.weathercode)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = emoji, fontSize = 38.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gió: ${weather.windspeed.toInt()} km/h",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Text(
                    text = "${weather.temperature.toInt()}°C",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ForestGreen,
                    fontWeight = FontWeight.Black
                )
            }
        }
        is Resource.Error -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "❌ Không thể tải thời tiết", color = DangerRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DashboardFarmItem(farm: Farm, variantName: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassCardBg)
            .border(1.dp, GlassCardBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Leaf/Crop Icon background circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(LightMint),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌾", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = farm.farmName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$variantName • ${farm.areaM2.toInt()} m²",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

        }
    }
}
