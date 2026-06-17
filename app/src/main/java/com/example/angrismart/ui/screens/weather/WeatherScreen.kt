package com.example.angrismart.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(GlassBgStart, GlassBgEnd)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Dự báo thời tiết", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về", tint = TextPrimary)
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Thẻ Thời Tiết Hiện Tại
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Hôm nay", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (current is Resource.Loading) {
                        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ForestGreen)
                        }
                    } else if (current is Resource.Success) {
                        val data = current.data
                        val (icon, desc) = WeatherViewModel.weatherDescription(data?.weathercode ?: 0)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassCardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(1.dp, GlassCardBorder, RoundedCornerShape(24.dp))
                                    .padding(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Vị trí của bạn", color = TextSecondary, fontSize = 14.sp)
                                        Text(
                                            text = "${data?.temperature}°C",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 52.sp,
                                            color = ForestGreen
                                        )
                                        Text(text = desc, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    }
                                    Text(text = icon, fontSize = 72.sp)
                                }
                            }
                        }
                    } else {
                        Text(text = current.message.toString(), color = DangerRed)
                    }
                }

                // Dự báo 7 ngày tới
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Dự báo 7 ngày tiếp theo", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (daily is Resource.Loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ForestGreen)
                        }
                    }
                } else if (daily is Resource.Success) {
                    val days = daily.data?.time ?: emptyList()
                    val tempsMax = daily.data?.tempMax ?: emptyList()
                    val tempsMin = daily.data?.tempMin ?: emptyList()
                    
                    items(days.size) { index ->
                        // Bỏ qua ngày hôm nay (index = 0) nếu muốn, làm mốc ngày mai
                        if (index > 0) {
                             Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = GlassCardBg)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, GlassCardBorder, RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val dateStr = days[index].substring(5) // Cắt "2026-" còn "03-29"
                                        
                                        Text(text = "📆 $dateStr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "❄️ ${tempsMin[index]}°C", color = Color.Blue.copy(alpha=0.7f), fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(text = "🔥 ${tempsMax[index]}°C", color = DangerRed.copy(alpha=0.7f), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
