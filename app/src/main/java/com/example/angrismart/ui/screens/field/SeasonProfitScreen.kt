package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.domain.model.Harvest
import com.example.angrismart.ui.theme.*
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.HarvestViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonProfitScreen(
    fieldId: String,
    harvestViewModel: HarvestViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToAddHarvest: () -> Unit = {}
) {
    val harvestState by harvestViewModel.fieldHarvestState.collectAsState()
    val vndFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    // Load data on screen launch
    LaunchedEffect(fieldId) {
        harvestViewModel.loadHarvestsByField(fieldId)
    }

    val harvests = (harvestState as? Resource.Success)?.data ?: emptyList()
    val totalProfit = harvests.sumOf { it.profit }
    val totalRevenue = harvests.sumOf { it.totalRevenue }
    val totalExpense = harvests.sumOf { it.totalExpense }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lợi Nhuận Mùa Vụ",
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
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddHarvest,
                containerColor = ForestGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(18.dp))
            ) {
                Text("📦 Ghi thu hoạch mới", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = NeutralBg
    ) { paddingValues ->
        when (harvestState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary dashboard card
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
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            EmptyHarvestPlaceholder(onNavigateToAddHarvest)
                        }
                    } else {
                        item {
                            Text(
                                text = "Lịch sử thu hoạch",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0D3321)
                                ),
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(harvests) { harvest ->
                            HarvestItemCard(harvest, vndFormat)
                        }
                    }
                }
            }
        }
    }
}

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
        listOf(Color(0xFF0D5C34), Color(0xFF1B5E20))
    // Red/crimson gradient if negative
    else
        listOf(Color(0xFF880E4F), Color(0xFFB71C1C))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp)
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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "$harvestCount vụ thu hoạch",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats summary Columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryStatColumn(
                        label = "📈 Tổng doanh thu",
                        value = "${vndFormat.format(totalRevenue.toLong())} đ",
                        valueColor = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(42.dp)
                            .background(Color.White.copy(alpha = 0.25f))
                    )
                    SummaryStatColumn(
                        label = "📉 Tổng chi phí",
                        value = "${vndFormat.format(totalExpense.toLong())} đ",
                        valueColor = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(16.dp))

                // Total Season Profits Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isProfitable) "✅ Lợi nhuận" else "❌ Lỗ vốn",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "${if (totalProfit >= 0) "+" else ""}${vndFormat.format(totalProfit.toLong())} đ",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 28.sp
                        )
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
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Black,
                color = valueColor
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HarvestItemCard(harvest: Harvest, vndFormat: NumberFormat) {
    val isProfitable = harvest.profit >= 0
    val profitColor = if (isProfitable) Color(0xFF2E7D32) else Color(0xFFC62828)
    val profitBgColor = if (isProfitable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    val dateStr = try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        sdf.format(harvest.harvestDate.toDate())
    } catch (e: Exception) { "---" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Accent indicator stripe based on profits/loss
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(profitColor)
            )
            Column(modifier = Modifier.padding(18.dp)) {
                // Header Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = harvest.cropSeason,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF263238)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🌾 ${harvest.variantName}  •  📅 $dateStr",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                        )
                    }
                    // Profits Tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = profitBgColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, profitColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "${if (isProfitable) "+" else ""}${vndFormat.format(harvest.profit.toLong())} đ",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = profitColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFECEFF1))
                Spacer(modifier = Modifier.height(14.dp))

                // Numerical Columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HarvestDetailStat("⚖️ Sản lượng", "${vndFormat.format(harvest.totalWeight.toLong())} kg")
                    HarvestDetailStat("💵 Giá bán", "${vndFormat.format(harvest.salePrice.toLong())} đ")
                    HarvestDetailStat("📈 Doanh thu", "${vndFormat.format(harvest.totalRevenue.toLong())} đ")
                    HarvestDetailStat("📉 Chi phí", "${vndFormat.format(harvest.totalExpense.toLong())} đ")
                }
            }
        }
    }
}

@Composable
private fun HarvestDetailStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray,
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Black,
                color = Color(0xFF37474F),
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun EmptyHarvestPlaceholder(onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📦", fontSize = 58.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Chưa có dữ liệu thu hoạch",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF263238)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ghi nhận vụ thu hoạch đầu tiên để bắt đầu theo dõi tiến độ lợi nhuận của ruộng.",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "📦 Ghi thu hoạch đầu tiên",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
