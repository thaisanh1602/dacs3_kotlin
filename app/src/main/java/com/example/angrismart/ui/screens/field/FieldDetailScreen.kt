package com.example.angrismart.ui.screens.field

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.domain.model.Farm
import com.example.angrismart.ui.theme.*
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.FieldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldDetailScreen(
    fieldId: String,
    viewModel: FieldViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToAddHarvest: () -> Unit = {},
    onNavigateToSeasonProfit: () -> Unit = {}
) {
    val farmsState by viewModel.farmsState.collectAsState()
    val farm = (farmsState.data ?: emptyList()).find { it.id == fieldId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = farm?.farmName ?: "Chi tiết ruộng",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Sửa */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = TextSecondary)
                    }
                }
            )
        },
        containerColor = NeutralBg
    ) { paddingValues ->
        if (farm == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                if (farmsState is Resource.Loading) {
                    CircularProgressIndicator(color = ForestGreen)
                } else {
                    Text("Không tìm thấy dữ liệu ruộng", fontWeight = FontWeight.Bold, color = TextSecondary)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Farm info card
            ModernInfoCard(farm)
            Spacer(modifier = Modifier.height(16.dp))

            // Growth progress card
            ModernGrowthCard(farm)
            Spacer(modifier = Modifier.height(20.dp))

            // Quick actions
            Text(
                text = "Hành động nhanh",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Scan action
            ActionButton(
                icon = Icons.Default.CameraAlt,
                label = "Quét Sâu Bệnh",
                subtitle = "Chẩn đoán bệnh cây bằng AI",
                containerColor = ForestGreen,
                onClick = onNavigateToScan
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Harvest action
            ActionButton(
                icon = Icons.Default.Inventory2,
                label = "Ghi nhận Thu hoạch",
                subtitle = "Thêm kết quả thu hoạch mới",
                containerColor = WarningAmber,
                onClick = onNavigateToAddHarvest
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Profit action (outlined)
            OutlinedButton(
                onClick = onNavigateToSeasonProfit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreen)
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Xem Lợi nhuận Mùa vụ",
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ModernInfoCard(farm: Farm) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LightMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Thông tin thửa ruộng",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = DividerLine
            )

            InfoRow(label = "Giống lúa", value = farm.varietyName, icon = "🌱")
            InfoRow(label = "Diện tích", value = "${farm.areaM2} m²", icon = "📐")
            InfoRow(
                label = "Tình trạng",
                value = if (farm.status == "active") "Đang canh tác" else "Đã thu hoạch",
                icon = if (farm.status == "active") "✅" else "📦"
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
fun ModernGrowthCard(farm: Farm) {
    val progress = (farm.ageDays.toFloat() / farm.totalGrowthDays.toFloat()).coerceIn(0f, 1f)
    val stageName = getStageName(farm.ageDays, farm.totalGrowthDays)
    val stageColor = getStageColor(farm.ageDays, farm.totalGrowthDays)
    val stages = listOf("Mạ non", "Đẻ nhánh", "Làm đòng", "Chín sáp", "Thu hoạch")
    val currentStageIndex = when {
        progress < 0.2f -> 0
        progress < 0.5f -> 1
        progress < 0.8f -> 2
        progress < 1.0f -> 3
        else -> 4
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiến độ tăng trưởng",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress timeline stepper
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(horizontal = 12.dp)
                        .background(NeutralBg, RoundedCornerShape(2.dp))
                )
                // Active fill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(4.dp)
                            .background(ForestGreen, RoundedCornerShape(2.dp))
                    )
                }
                // Step nodes
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
                                .size(if (isCurrent) 20.dp else 14.dp)
                                .background(
                                    color = when {
                                        isCurrent -> ForestGreen
                                        isActive -> MintGreen
                                        else -> DividerLine
                                    },
                                    shape = CircleShape
                                )
                                .then(
                                    if (isCurrent) Modifier.border(2.dp, LightMint, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stage labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stages.forEachIndexed { index, name ->
                    val isCurrent = index == currentStageIndex
                    Text(
                        text = name,
                        fontSize = 10.sp,
                        color = if (isCurrent) ForestGreen else TextSecondary,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatChip(label = "Đã sạ", value = "${farm.ageDays} ngày")
                StatChip(label = "Tổng thời gian", value = "${farm.totalGrowthDays} ngày")
                StatChip(label = "Còn lại", value = "${(farm.totalGrowthDays - farm.ageDays).coerceAtLeast(0)} ngày")
            }
        }
    }
}

@Composable
fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    subtitle: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
