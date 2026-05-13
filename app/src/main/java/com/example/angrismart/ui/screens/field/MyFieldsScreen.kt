package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.domain.model.Farm
import com.example.angrismart.ui.theme.GreenPrimary
import com.example.angrismart.ui.theme.GreenSecondary
import com.example.angrismart.ui.theme.YellowWarning
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.FieldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFieldsScreen(
    viewModel: FieldViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToAddField: () -> Unit = {},
    onNavigateToFieldDetail: (String) -> Unit = {}
) {
    // Quan sát dòng chảy dữ liệu thực từ Firestore
    val farmsState by viewModel.farmsState.collectAsState()
    val rawFarms = farmsState.data ?: emptyList()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cánh đồng của tôi", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddField,
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(72.dp) // Nút bấm siêu to
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm ruộng", modifier = Modifier.size(36.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (farmsState is Resource.Loading) {
                item {
                    CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
                }
            } else if (rawFarms.isEmpty() && farmsState is Resource.Success) {
                item {
                    Text(
                        text = "Ông/Bà chưa có cánh đồng nào. Bấm nút dấu + ngay để tạo mới nhé!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.padding(32.dp).fillMaxWidth()
                    )
                }
            } else {
                items(rawFarms) { farm ->
                    FarmCard(farm = farm, onClick = { onNavigateToFieldDetail(farm.id) })
                }
            }

            
            item { Spacer(modifier = Modifier.height(100.dp)) } // Cận đáy tránh bị dính nút nổi
        }
    }
}

@Composable
fun FarmCard(farm: Farm, onClick: () -> Unit) {
    val realAgeDays = farm.sowingDate?.let { date ->
        val diffInMillies = System.currentTimeMillis() - date.toDate().time
        java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillies).toInt().coerceAtLeast(0)
    } ?: farm.ageDays

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(Color(0xFFDCE775), Color(0xFF81C784)) // Vàng chanh sang Xanh lá mảnh ruộng
                    )
                )
        ) {
            // Hoa văn mảnh ruộng mờ mờ ở góc
            Text(
                text = "🌾",
                fontSize = 80.sp, // import error sp? Ensure inside composable Text accepts it or we added it
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 20.dp),
                color = Color.White.copy(alpha = 0.4f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = farm.farmName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Giống: ${farm.varietyName} • ${farm.areaM2} m²",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Nổi bật giai đoạn
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = getStageName(realAgeDays, farm.totalGrowthDays),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = getStageColor(realAgeDays, farm.totalGrowthDays),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Giai đoạn bằng text ở đáy
                Text(
                    text = "Đã sạ: $realAgeDays / ${farm.totalGrowthDays} ngày",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1B5E20),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Hàm tính tự động Giai đoạn trưởng thành của lúa
fun getStageName(ageDays: Int, totalDays: Int): String {
    val progress = ageDays.toFloat() / totalDays.toFloat()
    return when {
        progress < 0.2f -> "Mạ non"
        progress < 0.5f -> "Đẻ nhánh"
        progress < 0.8f -> "Làm đòng"
        progress < 1.0f -> "Chín sáp"
        else -> "Đã thu hoạch"
    }
}

fun getStageColor(ageDays: Int, totalDays: Int): Color {
    val progress = ageDays.toFloat() / totalDays.toFloat()
    return when {
        progress < 0.5f -> GreenSecondary
        progress < 0.8f -> GreenPrimary
        progress < 1.0f -> YellowWarning
        else -> Color.Gray
    }
}
