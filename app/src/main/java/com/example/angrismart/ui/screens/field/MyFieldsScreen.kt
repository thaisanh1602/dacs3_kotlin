package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.domain.model.Farm
import com.example.angrismart.ui.theme.*
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
    val farmsState by viewModel.farmsState.collectAsState()
    val rawFarms = farmsState.data ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cánh đồng của tôi",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddField,
                containerColor = ForestGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(18.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm ruộng", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm Ruộng", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        containerColor = NeutralBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (farmsState is Resource.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ForestGreen)
                    }
                }
            } else if (rawFarms.isEmpty() && farmsState is Resource.Success) {
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(LightMint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Grass,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Chưa có cánh đồng nào",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nhấn nút \"Thêm Ruộng\" để bắt đầu quản lý canh tác",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            } else {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(rawFarms) { farm ->
                    ModernFarmCard(farm = farm, onClick = { onNavigateToFieldDetail(farm.id) })
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun ModernFarmCard(farm: Farm, onClick: () -> Unit) {
    val progressValue = (farm.ageDays.toFloat() / farm.totalGrowthDays.toFloat()).coerceIn(0f, 1f)
    val stageName = getStageName(farm.ageDays, farm.totalGrowthDays)
    val stageColor = getStageColor(farm.ageDays, farm.totalGrowthDays)

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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left – green icon box
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightMint),
                contentAlignment = Alignment.Center
            ) {
                Text("🌾", fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center – farm info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = farm.farmName,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${farm.varietyName} · ${farm.areaM2} m²",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Progress bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ngày ${farm.ageDays}/${farm.totalGrowthDays}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${(progressValue * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ForestGreen,
                        trackColor = NeutralBg
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right – stage badge + chevron
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = stageColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stageName,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = stageColor,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
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

// Stage name evaluation helper
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

// Stage color evaluation helper
fun getStageColor(ageDays: Int, totalDays: Int): Color {
    val progress = ageDays.toFloat() / totalDays.toFloat()
    return when {
        progress < 0.5f -> MintGreen
        progress < 0.8f -> ForestGreen
        progress < 1.0f -> WarningAmber
        else -> TextSecondary
    }
}
