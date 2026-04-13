package com.example.angrismart.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.ui.theme.GreenPrimary
import com.example.angrismart.ui.theme.GreenSecondary
import com.example.angrismart.ui.theme.RedError
import com.example.angrismart.ui.theme.YellowWarning
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    weatherViewModel: WeatherViewModel = viewModel(),
    userName: String = "Bà con",
    onNavigateToFields: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToWeather: () -> Unit = {},
    onNavigateToChat: () -> Unit = {}
) {
    val weatherState by weatherViewModel.currentWeather.collectAsState()
    val diseaseRisk by weatherViewModel.diseaseRisk.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AngriSmart", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Lời chào và Thẻ thời tiết THẬT từ API
            GreetingSection(userName, weatherState)

            Spacer(modifier = Modifier.height(16.dp))

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

            Text(
                text = "Tính năng chính",
                style = MaterialTheme.typography.titleLarge,
                color = GreenPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Lưới các chức năng lớn
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MenuCard(
                        title = "Quản lý\nĐồng Ruộng",
                        emoji = "🌾",
                        backgroundColor = GreenSecondary,
                        onClick = onNavigateToFields
                    )
                }
                item {
                    MenuCard(
                        title = "Quét\nSâu Bệnh",
                        emoji = "📷",
                        backgroundColor = GreenPrimary,
                        textColor = Color.White,
                        onClick = onNavigateToScan
                    )
                }
                item {
                    MenuCard(
                        title = "Thời tiết\nNông Vụ",
                        emoji = "⛅",
                        backgroundColor = Color(0xFF4FC3F7),
                        onClick = onNavigateToWeather
                    )
                }
                item {
                    MenuCard(
                        title = "Hỏi đáp\nChuyên gia AI",
                        emoji = "🤖",
                        backgroundColor = YellowWarning,
                        onClick = onNavigateToChat
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingSection(userName: String, weatherState: Resource<com.example.angrismart.data.remote.model.CurrentWeather>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Chào anh/chú,",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                color = GreenPrimary,
                fontSize = 32.sp
            )
        }

        // Thẻ thời tiết THẬT từ OpenMeteo
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            shape = RoundedCornerShape(16.dp)
        ) {
            when (weatherState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp).size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                is Resource.Success -> {
                    val weather = weatherState.data!!
                    val (emoji, desc) = WeatherViewModel.weatherDescription(weather.weathercode)
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${weather.temperature.toInt()}°C",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = "❌",
                        fontSize = 28.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AlertBanner(message: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Cảnh báo",
                tint = RedError,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = RedError,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    emoji: String,
    backgroundColor: Color,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 54.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
        }
    }
}
