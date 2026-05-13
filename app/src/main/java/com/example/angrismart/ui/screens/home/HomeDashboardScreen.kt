package com.example.angrismart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import com.example.angrismart.ui.theme.GreenPrimary
import com.example.angrismart.ui.theme.GreenSecondary
import com.example.angrismart.ui.theme.RedError
import com.example.angrismart.ui.theme.YellowWarning
import com.example.angrismart.ui.theme.BackgroundLight
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.WeatherViewModel
import com.example.angrismart.viewmodel.HarvestViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    weatherViewModel: WeatherViewModel = viewModel(),
    userName: String = "Bà con",
    onNavigateToFields: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToWeather: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfit: () -> Unit = {},
    harvestViewModel: HarvestViewModel = viewModel()
) {
    val weatherState by weatherViewModel.currentWeather.collectAsState()
    val diseaseRisk by weatherViewModel.diseaseRisk.collectAsState()
    
    val harvestState by harvestViewModel.harvestListState.collectAsState()
    
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
        harvestViewModel.loadHarvestsByUser()
        
        // Location logic
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
    
    val harvests = (harvestState as? Resource.Success)?.data ?: emptyList()
    val totalProfit = harvests.sumOf { it.profit }
    val totalRevenue = harvests.sumOf { it.totalRevenue }
    val totalExpense = harvests.sumOf { it.totalExpense }
    val vndFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
    
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Header Section with Gradient and Weather Card
            HeaderSection(userName, weatherState)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Banner cảnh báo bệnh (CHỈ HIỆN NẾU CÓ NGUY CƠ)
                diseaseRisk?.let { risk ->
                    if (risk.hasRisk) {
                        AlertBanner(
                            message = "⚠️ ${risk.diseaseName}\nDự kiến ngày ${risk.riskDay}",
                            onClick = { /* Mở chi tiết cảnh báo */ }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Global Profit Summary Card
                Spacer(modifier = Modifier.height(16.dp))
                val vndFormatLocale = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("vi").setRegion("VN").build())
                HomeProfitSummaryCard(
                    totalRevenue = totalRevenue,
                    totalExpense = totalExpense,
                    totalProfit = totalProfit,
                    harvestCount = harvests.size,
                    vndFormat = vndFormatLocale,
                    onClick = onNavigateToProfit
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tính năng nổi bật",
                    style = MaterialTheme.typography.titleLarge,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Tính năng chính: Quét Sâu Bệnh
                HeroFeatureCard(
                    title = "Quét\nSâu Bệnh",
                    subtitle = "Sử dụng AI để nhận diện bệnh",
                    emoji = "📷",
                    gradient = Brush.linearGradient(
                        colors = listOf(GreenPrimary, Color(0xFF43A047))
                    ),
                    onClick = onNavigateToScan
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Các tính năng phụ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SmallFeatureCard(
                        title = "Đồng Ruộng",
                        emoji = "🌾",
                        gradient = Brush.linearGradient(
                            colors = listOf(GreenSecondary, Color(0xFFA5D6A7))
                        ),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToFields
                    )

                    SmallFeatureCard(
                        title = "Thời tiết",
                        emoji = "⛅",
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFF29B6F6), Color(0xFF81D4FA))
                        ),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWeather
                    )

                    SmallFeatureCard(
                        title = "Lợi nhuận",
                        emoji = "📊",
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFF8E24AA), Color(0xFFCE93D8))
                        ),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProfit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hỏi đáp AI Full width nhỏ
                MediumFeatureCard(
                    title = "Hỏi đáp Chuyên gia AI",
                    subtitle = "Giải đáp mọi thắc mắc nông nghiệp",
                    emoji = "🤖",
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFF9A825), YellowWarning)
                    ),
                    onClick = onNavigateToChat
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, weatherState: Resource<com.example.angrismart.data.remote.model.CurrentWeather>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // Background Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B5E20), GreenPrimary)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Text(
                    text = "Chào buổi sáng,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Thẻ thời tiết đè lên viền dưới của Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
        ) {
            WeatherCard(weatherState)
        }
    }
}

@Composable
fun WeatherCard(weatherState: Resource<com.example.angrismart.data.remote.model.CurrentWeather>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (weatherState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = GreenPrimary,
                        strokeWidth = 3.dp
                    )
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
                            Text(text = emoji, fontSize = 42.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF424242),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gió: ${weather.windspeed.toInt()} km/h",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                        Text(
                            text = "${weather.temperature.toInt()}°C",
                            style = MaterialTheme.typography.headlineLarge,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                is Resource.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "❌", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Không thể tải thời tiết",
                            color = RedError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeProfitSummaryCard(
    totalRevenue: Double,
    totalExpense: Double,
    totalProfit: Double,
    harvestCount: Int,
    vndFormat: java.text.NumberFormat,
    onClick: () -> Unit = {}
) {
    val isProfitable = totalProfit >= 0
    val gradientColors = if (isProfitable)
        listOf(Color(0xFF1B5E20), Color(0xFF388E3C))
    else
        listOf(Color(0xFF880E4F), Color(0xFFC62828))

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = gradientColors))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰 Tổng kết Lợi Nhuận",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "$harvestCount vụ thu hoạch",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Doanh thu & Chi phí
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HomeSummaryStatColumn(
                        label = "📈 Tổng doanh thu",
                        value = "${vndFormat.format(totalRevenue.toLong())} đ",
                        valueColor = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    HomeSummaryStatColumn(
                        label = "📉 Tổng chi phí",
                        value = "${vndFormat.format(totalExpense.toLong())} đ",
                        valueColor = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                // Lợi nhuận tổng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isProfitable) "✅ Lợi nhuận" else "❌ Lỗ vốn",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (totalProfit >= 0) "+" else ""}${vndFormat.format(totalProfit.toLong())} đ",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSummaryStatColumn(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AlertBanner(message: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
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
                tint = RedError,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = RedError,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun HeroFeatureCard(
    title: String,
    subtitle: String,
    emoji: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Text(
                text = emoji,
                fontSize = 64.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun SmallFeatureCard(
    title: String,
    emoji: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MediumFeatureCard(
    title: String,
    subtitle: String,
    emoji: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 40.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
