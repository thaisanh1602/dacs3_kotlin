package com.example.angrismart.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import com.example.angrismart.ui.theme.GreenPrimary
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
                title = { Text("Dự báo thời tiết", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Thẻ Thời Tiết Hiện Tại
                item {
                    Text(text = "Hôm nay", style = MaterialTheme.typography.titleLarge, color = GreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (current is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (current is Resource.Success) {
                        val data = current.data
                        val (icon, desc) = WeatherViewModel.weatherDescription(data?.weathercode ?: 0)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "TP.Hồ Chí Minh", color = Color.Gray, fontSize = 16.sp)
                                    Text(
                                        text = "${data?.temperature}°C",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 56.sp,
                                        color = GreenPrimary
                                    )
                                    Text(text = desc, style = MaterialTheme.typography.titleMedium, color = Color.DarkGray)
                                }
                                Text(text = icon, fontSize = 80.sp)
                            }
                        }
                    } else {
                        Text(text = current.message.toString(), color = Color.Red)
                    }
                }

                // Dự báo 7 ngày tới
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Dự báo 7 ngày tiếp theo", style = MaterialTheme.typography.titleLarge, color = GreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (daily is Resource.Loading) {
                    item { CircularProgressIndicator() }
                } else if (daily is Resource.Success) {
                    val days = daily.data?.time ?: emptyList()
                    val tempsMax = daily.data?.tempMax ?: emptyList()
                    val tempsMin = daily.data?.tempMin ?: emptyList()
                    
                    items(days.size) { index ->
                        // Bỏ qua ngày hôm nay (index = 0) nếu muốn, làm mốc ngày mai
                        if (index > 0) {
                             Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val dateStr = days[index].substring(5) // Cắt "2026-" còn "03-29"
                                    
                                    Text(text = "📆 $dateStr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "❄️ ${tempsMin[index]}°C", color = Color.Blue.copy(alpha=0.7f), fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(text = "🔥 ${tempsMax[index]}°C", color = Color.Red.copy(alpha=0.7f), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}
