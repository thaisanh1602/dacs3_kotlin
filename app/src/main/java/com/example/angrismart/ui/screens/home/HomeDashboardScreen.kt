package com.example.angrismart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.ui.theme.*
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.WeatherViewModel
import java.util.Calendar

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
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(NeutralBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── Header ──────────────────────────────────────────────────────
            HomeHeaderSection(userName = userName, weatherState = weatherState)

            // ── Content ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Disease risk alert (shows only when risk exists)
                diseaseRisk?.let { risk ->
                    if (risk.hasRisk) {
                        RiskAlertBanner(
                            message = "${risk.diseaseName} – Dự kiến ngày ${risk.riskDay}",
                            onClick = {}
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Section header
                Text(
                    text = "Tính năng nổi bật",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Primary CTA – Scan Disease
                PrimaryFeatureCard(
                    icon = Icons.Default.CameraAlt,
                    title = "Quét Sâu Bệnh",
                    subtitle = "AI nhận diện bệnh hại cây trồng chính xác",
                    accentColor = ForestGreen,
                    onClick = onNavigateToScan
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Two small cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SecondaryFeatureCard(
                        icon = Icons.Default.Grass,
                        title = "Đồng Ruộng",
                        subtitle = "Quản lý mùa vụ",
                        accentColor = SageGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToFields
                    )
                    SecondaryFeatureCard(
                        icon = Icons.Default.WbSunny,
                        title = "Thời Tiết",
                        subtitle = "Dự báo 7 ngày",
                        accentColor = Color(0xFF0EA5E9),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWeather
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Chat AI card
                ChatFeatureCard(onClick = onNavigateToChat)

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun HomeHeaderSection(
    userName: String,
    weatherState: Resource<com.example.angrismart.data.remote.model.CurrentWeather>
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Chào buổi sáng"
        in 12..17 -> "Chào buổi chiều"
        else -> "Chào buổi tối"
    }
    val greetingEmoji = when (hour) {
        in 0..11 -> "🌅"
        in 12..17 -> "☀️"
        else -> "🌙"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1B4332), ForestGreen, SageGreen)
                    )
                )
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(x = 240.dp, y = (-30).dp)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 270.dp, y = 60.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
            )

            // Greeting text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 60.dp)
            ) {
                Text(
                    text = "$greeting $greetingEmoji",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            // App logo top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 52.dp, end = 20.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Weather card overlapping bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp)
        ) {
            CompactWeatherCard(weatherState)
        }
    }
}

@Composable
fun CompactWeatherCard(weatherState: Resource<com.example.angrismart.data.remote.model.CurrentWeather>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            when (weatherState) {
                is Resource.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ForestGreen,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Đang tải thời tiết...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
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
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "💨 ${weather.windspeed.toInt()} km/h",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        Text(
                            text = "${weather.temperature.toInt()}°C",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = ForestGreen
                        )
                    }
                }
                is Resource.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("⚠️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Không thể tải thời tiết",
                            color = DangerRed,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RiskAlertBanner(message: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFEBEB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cảnh báo sâu bệnh",
                    style = MaterialTheme.typography.labelMedium,
                    color = DangerRed,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = DangerRed.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = DangerRed.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PrimaryFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.75f))
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Decorative circle behind icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
            }
            // Chevron arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
            )
        }
    }
}

@Composable
fun SecondaryFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(6.dp, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun ChatFeatureCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hỏi đáp Chuyên gia AI",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Giải đáp mọi thắc mắc nông nghiệp 24/7",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NeutralBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
