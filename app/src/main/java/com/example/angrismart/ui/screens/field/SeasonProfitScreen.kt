package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.domain.model.Harvest
import com.example.angrismart.domain.model.RiceVariant
import com.example.angrismart.domain.model.SeasonTemplate
import com.example.angrismart.ui.theme.GreenPrimary
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.HarvestViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonProfitScreen(
    harvestViewModel: HarvestViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToAddHarvest: () -> Unit = {}
) {
    val harvestState by harvestViewModel.harvestListState.collectAsState()
    val riceVariantsState by harvestViewModel.riceVariantsState.collectAsState()
    val seasonTemplatesState by harvestViewModel.seasonTemplatesState.collectAsState()
    
    val riceVariants: List<RiceVariant> = riceVariantsState.data ?: emptyList()
    val seasons: List<SeasonTemplate> = seasonTemplatesState.data ?: emptyList()
    
    val vndFormat = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")) }

    // Khi màn hình mở, load dữ liệu thu hoạch TOÀN BỘ của user
    LaunchedEffect(Unit) {
        harvestViewModel.loadHarvestsByUser()
        harvestViewModel.loadRiceVariants()
        harvestViewModel.loadSeasonTemplates()
    }

    val harvests = (harvestState as? Resource.Success)?.data ?: emptyList()
    val totalProfit = harvests.sumOf { it.profit }
    val totalRevenue = harvests.sumOf { it.totalRevenue }
    val totalExpense = harvests.sumOf { it.totalExpense }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê Lợi nhuận", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddHarvest,
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("📦 Ghi thu hoạch mới", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        when (harvestState) {
            is Resource.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- Thẻ tổng hợp lợi nhuận ---
                    item {
                        ProfitSummaryCard(
                            totalRevenue = totalRevenue,
                            totalExpense = totalExpense,
                            totalProfit = totalProfit,
                            harvestCount = harvests.size,
                            vndFormat = vndFormat
                        )
                    }

                    if (harvests.isEmpty()) {
                        item { EmptyHarvestPlaceholder(onNavigateToAddHarvest) }
                    } else {
                        // --- Biểu đồ cột ---
                        item {
                            ProfitBarChart(harvests = harvests, seasons = seasons)
                        }

                        item {
                            Text(
                                "Lịch sử thu hoạch",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(harvests) { harvest ->
                            val riceName = riceVariants.find { it.id == harvest.variantId }?.name ?: harvest.variantId
                            val seasonName = seasons.find { it.id == harvest.seasonId }?.seasonName ?: harvest.seasonId
                            HarvestItemCard(harvest, riceName, seasonName, vndFormat)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-components
// ---------------------------------------------------------------------------

@Composable
private fun ProfitSummaryCard(
    totalRevenue: Double,
    totalExpense: Double,
    totalProfit: Double,
    harvestCount: Int,
    vndFormat: NumberFormat
) {
    val isProfitable = totalProfit >= 0
    val gradientColors = if (isProfitable)
        listOf(Color(0xFF1B5E20), Color(0xFF388E3C))
    else
        listOf(Color(0xFF880E4F), Color(0xFFC62828))

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        text = "💰 Tổng kết ruộng",
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
                    SummaryStatColumn(
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
                    SummaryStatColumn(
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
private fun SummaryStatColumn(label: String, value: String, valueColor: Color) {
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
private fun HarvestItemCard(harvest: Harvest, riceName: String, seasonName: String, vndFormat: NumberFormat) {
    val isProfitable = harvest.profit >= 0
    val profitColor = if (isProfitable) Color(0xFF2E7D32) else Color(0xFFC62828)
    val profitBgColor = if (isProfitable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    val dateStr = try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.Builder().setLanguage("vi").setRegion("VN").build())
        harvest.harvestDate?.let { sdf.format(it.toDate()) } ?: "---"
    } catch (e: Exception) { "---" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: tên vụ + ngày
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = seasonName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🌾 $riceName  •  📅 $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                // Badge lợi nhuận
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = profitBgColor
                ) {
                    Text(
                        text = "${if (isProfitable) "+" else ""}${vndFormat.format(harvest.profit.toLong())} đ",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = profitColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Chi tiết số liệu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HarvestDetailStat("⚖️ Sản lượng", "${vndFormat.format(harvest.totalWeight.toLong())} kg")
                HarvestDetailStat("💵 Giá bán", "${vndFormat.format(harvest.salePrice.toLong())} đ/kg")
                HarvestDetailStat("📈 Doanh thu", "${vndFormat.format(harvest.totalRevenue.toLong())} đ")
                HarvestDetailStat("📉 Chi phí", "${vndFormat.format(harvest.totalExpense.toLong())} đ")
            }
        }
    }
}

@Composable
private fun HarvestDetailStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun EmptyHarvestPlaceholder(onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📦", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Chưa có dữ liệu thu hoạch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Ghi nhận vụ thu hoạch đầu tiên để\nbắt đầu theo dõi lợi nhuận",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📦 Ghi thu hoạch đầu tiên", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfitBarChart(harvests: List<Harvest>, seasons: List<com.example.angrismart.domain.model.SeasonTemplate>, modifier: Modifier = Modifier) {
    if (harvests.isEmpty()) return

    // Sắp xếp theo ngày và lấy tối đa 6 vụ gần nhất (Sử dụng seconds để tránh crash nếu date null)
    val sortedHarvests = harvests.sortedBy { it.harvestDate?.seconds ?: 0L }.takeLast(6)
    val maxAbsValue = sortedHarvests.maxOfOrNull { Math.abs(it.profit) } ?: 0.0
    val range = if (maxAbsValue > 0) maxAbsValue * 1.5 else 1.0 // Tránh chia cho 0

    val vndFormatShort = remember { NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")) }

    Card(
        modifier = modifier.fillMaxWidth().height(240.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Biểu đồ Lợi nhuận (6 vụ gần nhất)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                sortedHarvests.forEach { harvest ->
                    val isPositive = harvest.profit >= 0
                    val fraction = (Math.abs(harvest.profit) / range).toFloat().coerceIn(0.05f, 0.8f)
                    val barColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
                    
                    val fullName = seasons.find { it.id == harvest.seasonId }?.seasonName ?: harvest.seasonId
                    val seasonAbbr = fullName.split(" ").map { it.firstOrNull()?.uppercase() ?: "" }.joinToString("").take(4)
                    
                    // Rút gọn số tiền hiển thị trên cột (ví dụ: 1.2M hoặc 500k)
                    val profitValue = harvest.profit
                    val displayValue = when {
                        Math.abs(profitValue) >= 1_000_000 -> "${(profitValue / 1_000_000).toInt()}Tr"
                        Math.abs(profitValue) >= 1_000 -> "${(profitValue / 1_000).toInt()}k"
                        else -> "${profitValue.toInt()}"
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        // Giá trị tiền trên đầu cột
                        Text(
                            text = "${if (isPositive) "+" else ""}$displayValue",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = barColor,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        // Thanh cột
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .fillMaxHeight(fraction)
                                .background(barColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Nhãn mùa vụ
                        Text(
                            text = seasonAbbr,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
