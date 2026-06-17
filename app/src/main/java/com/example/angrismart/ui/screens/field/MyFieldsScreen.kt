package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.domain.model.Farm
import com.example.angrismart.domain.model.RiceVariant
import com.example.angrismart.ui.theme.*
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.FieldViewModel
import com.example.angrismart.ui.screens.home.getFarmHealth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFieldsScreen(
    viewModel: FieldViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToAddField: () -> Unit = {},
    onNavigateToFieldDetail: (String) -> Unit = {}
) {
    val farmsState by viewModel.farmsState.collectAsState()
    val riceVariantsState by viewModel.riceVariantsState.collectAsState()
    val rawFarms = farmsState.data ?: emptyList()
    val variants: List<RiceVariant> = riceVariantsState.data ?: emptyList()

    LaunchedEffect(Unit) {
        viewModel.loadFarms()
        viewModel.loadRiceVariants()
    }

    val totalFarms = rawFarms.size
    val needyFarmsCount = rawFarms.count { getFarmHealth(it) < 60 }
    val healthyRatio = if (totalFarms > 0) (totalFarms - needyFarmsCount).toFloat() / totalFarms.toFloat() else 1f

    // Filter states
    var filterNeedsCareOnly by remember { mutableStateOf(false) }

    val displayedFarms = remember(rawFarms, filterNeedsCareOnly) {
        if (filterNeedsCareOnly) {
            rawFarms.filter { getFarmHealth(it) < 60 }
        } else {
            rawFarms
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(GlassBgStart, GlassBgEnd)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                MediumTopAppBar(
                    title = {
                        Text(
                            "Đồng ruộng của tôi",
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontSize = 24.sp
                        )
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.Transparent),
                    actions = {
                        IconButton(onClick = { /* Search */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = TextPrimary)
                        }
                        IconButton(onClick = onNavigateToAddField) {
                            Icon(Icons.Default.Add, contentDescription = "Thêm ruộng", tint = TextPrimary)
                        }
                        IconButton(onClick = { /* Settings/Options */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = TextPrimary)
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. STATS OVERVIEW CARD (Span both columns)
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassCardBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GlassCardBorder, RoundedCornerShape(24.dp))
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Custom Circular Progress Ring indicating healthy crops ratio
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { healthyRatio },
                                            modifier = Modifier.fillMaxSize(),
                                            color = ForestGreen,
                                            strokeWidth = 5.dp,
                                            trackColor = Color(0xFFE8F5E9),
                                        )
                                        Text(
                                            text = "$totalFarms",
                                            fontWeight = FontWeight.Black,
                                            color = ForestGreen,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "$totalFarms ruộng lúa",
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        if (needyFarmsCount > 0) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = DangerRed,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "$needyFarmsCount ruộng cần chăm sóc",
                                                    color = DangerRed,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "Tất cả ruộng lúa đều khỏe mạnh",
                                                color = ForestGreen,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                // Quick filter toggle / icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(LightMint)
                                        .clickable { onNavigateToAddField() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌾", fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }

                // 2. FILTER CHIPS ROW (Span both columns)
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !filterNeedsCareOnly,
                            onClick = { filterNeedsCareOnly = false },
                            label = { Text("Tất cả ruộng") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = filterNeedsCareOnly,
                            onClick = { filterNeedsCareOnly = true },
                            label = { Text("Cần chăm sóc") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DangerRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 3. RICE FIELDS GRID ITEMS
                if (farmsState is Resource.Loading) {
                    item(span = { GridItemSpan(2) }) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ForestGreen)
                        }
                    }
                } else if (displayedFarms.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (filterNeedsCareOnly) "Không có ruộng nào cần chăm sóc lúc này!" else "Bạn chưa tạo cánh đồng nào. Hãy tạo mới nhé!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(displayedFarms) { farm ->
                        val variantName = variants.find { it.id == farm.varietyId }?.name ?: farm.varietyId
                        GridFarmCard(
                            farm = farm,
                            variantName = variantName,
                            onClick = { onNavigateToFieldDetail(farm.id) }
                        )
                    }
                }

                // Spacing bottom
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun GridFarmCard(farm: Farm, variantName: String, onClick: () -> Unit) {
    val health = getFarmHealth(farm)
    val healthColor = when {
        health < 60 -> DangerRed
        health < 75 -> WarningAmber
        else -> ForestGreen
    }

    val realAgeDays = farm.sowingDate?.let { date ->
        val diffInMillies = System.currentTimeMillis() - date.toDate().time
        java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillies).toInt().coerceAtLeast(0)
    } ?: farm.ageDays

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
        ) {
            // Gradient top plate mimicking picture backgrounds
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌾",
                    fontSize = 44.sp
                )
                
                // Circular health indicator overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 8.dp, end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { health.toFloat() / 100f },
                        modifier = Modifier.size(32.dp),
                        color = healthColor,
                        strokeWidth = 3.dp,
                        trackColor = Color(0xFFEEEEEE)
                    )
                    Text(
                        text = "$health",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = healthColor
                    )
                }
            }

            // Bottom text description area
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = farm.farmName,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = variantName,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ngày thứ $realAgeDays",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    
                    // Simple growth phase text badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LightMint)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = getStageName(realAgeDays, farm.totalGrowthDays),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }
                }
            }
        }
    }
}

fun getStageName(ageDays: Int, totalDays: Int): String {
    val safeTotal = if (totalDays > 0) totalDays else 100
    val progress = ageDays.toFloat() / safeTotal.toFloat()
    return when {
        progress < 0.2f -> "Mạ non"
        progress < 0.5f -> "Đẻ nhánh"
        progress < 0.8f -> "Làm đòng"
        progress < 1.0f -> "Chín sáp"
        else -> "Đã thu hoạch"
    }
}

fun getStageColor(ageDays: Int, totalDays: Int): Color {
    val safeTotal = if (totalDays > 0) totalDays else 100
    val progress = ageDays.toFloat() / safeTotal.toFloat()
    return when {
        progress < 0.5f -> MintGreen
        progress < 0.8f -> ForestGreen
        progress < 1.0f -> WarningAmber
        else -> Color.Gray
    }
}
