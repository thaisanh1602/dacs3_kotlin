package com.example.angrismart.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.ui.theme.*
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val current by viewModel.currentWeather.collectAsState()
    val daily by viewModel.dailyForecast.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dự báo thời tiết",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Section: Today
            item {
                Text(
                    text = "Hôm nay",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                when (current) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ForestGreen)
                        }
                    }
                    is Resource.Success -> {
                        val data = current.data
                        val (icon, desc) = WeatherViewModel.weatherDescription(data?.weathercode ?: 0)

                        // Hero weather card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(10.dp, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF1B4332),
                                                ForestGreen,
                                                SageGreen
                                            )
                                        )
                                    )
                                    .padding(24.dp)
                            ) {
                                // Decorative circle
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 30.dp, y = (-30).dp)
                                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(100))
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "TP. Hồ Chí Minh",
                                            color = Color.White.copy(alpha = 0.8f),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "${data?.temperature?.toInt()}°C",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 56.sp,
                                            color = Color.White,
                                            lineHeight = 60.sp
                                        )
                                        Text(
                                            text = desc,
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            WeatherInfoChip(
                                                label = "Gió",
                                                value = "${data?.windspeed?.toInt()} km/h"
                                            )
                                        }
                                    }
                                    Text(
                                        text = icon,
                                        fontSize = 72.sp
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F0))
                        ) {
                            Text(
                                text = "⚠️ ${current.message}",
                                color = DangerRed,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Section: 7-day forecast
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dự báo 7 ngày tới",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (daily is Resource.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ForestGreen)
                    }
                }
            } else if (daily is Resource.Success) {
                val days = daily.data?.time ?: emptyList()
                val tempsMax = daily.data?.tempMax ?: emptyList()
                val tempsMin = daily.data?.tempMin ?: emptyList()

                items(days.size) { index ->
                    if (index > 0) {
                        DayForecastCard(
                            date = days[index].substring(5),
                            tempMin = tempsMin.getOrNull(index)?.toInt() ?: 0,
                            tempMax = tempsMax.getOrNull(index)?.toInt() ?: 0
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun WeatherInfoChip(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = "$label: $value",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DayForecastCard(date: String, tempMin: Int, tempMax: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ngày $date",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Min temperature chip (blue)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE0F2FE))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$tempMin°C",
                        color = Color(0xFF0369A1),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                // Max temperature chip (red)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFEBEB))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$tempMax°C",
                        color = DangerRed,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
