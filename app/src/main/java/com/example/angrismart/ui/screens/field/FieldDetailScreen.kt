package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.util.Calendar
import java.util.Date
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.domain.model.Farm
import com.example.angrismart.domain.model.RiceVariant
import com.example.angrismart.ui.theme.*
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.FieldViewModel
import com.example.angrismart.viewmodel.FinancialTransactionViewModel
import com.example.angrismart.ui.screens.home.getFarmHealth
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldDetailScreen(
    fieldId: String,
    viewModel: FieldViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToAddHarvest: () -> Unit = {},
    onNavigateToScanResult: () -> Unit = {}, // Optional backward-compatibility
    onNavigateToAddTransaction: () -> Unit = {},
    transactionViewModel: FinancialTransactionViewModel = viewModel()
) {
    val farmsState by viewModel.farmsState.collectAsState()
    val riceVariantsState by viewModel.riceVariantsState.collectAsState()
    
    val farm = (farmsState.data ?: emptyList()).find { it.id == fieldId }
    val variants: List<RiceVariant> = riceVariantsState.data ?: emptyList()
    val variantName = variants.find { it.id == farm?.varietyId }?.name ?: farm?.varietyId ?: "---"

    // Load expenses
    val transactionsState by transactionViewModel.transactions.collectAsState()
    LaunchedEffect(fieldId) {
        if (fieldId.isNotEmpty()) {
            transactionViewModel.getTransactionsByField(fieldId)
        }
    }

    val totalExpense = remember(transactionsState) {
        if (transactionsState is Resource.Success) {
            transactionsState.data?.filter { it.type == "expense" }?.sumOf { it.price } ?: 0.0
        } else 0.0
    }

    val vndFormat = remember {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = farm?.sowingDate?.toDate()?.time ?: System.currentTimeMillis()
    )

    // Like state simulation
    var isLiked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(GlassBgStart, GlassBgEnd)))
    ) {
        if (farm == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (farmsState is Resource.Loading) {
                    CircularProgressIndicator(color = ForestGreen)
                } else {
                    Text("Không tìm thấy dữ liệu ruộng", color = TextPrimary)
                }
            }
            return@Box
        }

        val health = getFarmHealth(farm)
        val healthColor = when {
            health < 60 -> DangerRed
            health < 75 -> WarningAmber
            else -> ForestGreen
        }
        val healthText = when {
            health < 60 -> "Cần chăm sóc"
            health < 75 -> "Ổn định"
            else -> "Khỏe mạnh"
        }

        val realAgeDays = farm.sowingDate?.let { date ->
            val diffInMillies = System.currentTimeMillis() - date.toDate().time
            java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillies).toInt().coerceAtLeast(0)
        } ?: farm.ageDays

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. HERO HEADER AREA WITH IMAGE & OVERLAYS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {
                // Crop image replacement (Gradient background mimicking image visual)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF2E7D32), Color(0xFF81C784))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Spa,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = Color.White.copy(alpha = 0.35f)
                    )
                }

                // Top icons panel (Back, Edit, Settings)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở lại", tint = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Sửa ngày gieo", tint = Color.White)
                        }
                        IconButton(
                            onClick = { /* More details */ },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = Color.White)
                        }
                    }
                }

                // Heart/Like Badge Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 180.dp, end = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { isLiked = !isLiked }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Thích",
                            tint = if (isLiked) DangerRed else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLiked) "97" else "96",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // FLOATING CARD: Field variety name & basic stats
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassCardBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = farm.farmName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = variantName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Stats icons row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WaterDrop, contentDescription = "Chi phí", tint = InfoBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${vndFormat.format(totalExpense)} đ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = "Diện tích", tint = ForestGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${farm.areaM2.toInt()} m²",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, contentDescription = "Sức khỏe", tint = healthColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$health Sức khỏe",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 2. PLANT HEALTH CARD (Circular score rating)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassCardBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "Sức khỏe ruộng lúa",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Health Circle
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(68.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { health.toFloat() / 100f },
                                            modifier = Modifier.fillMaxSize(),
                                            color = healthColor,
                                            strokeWidth = 6.dp,
                                            trackColor = Color(0xFFEEEEEE)
                                        )
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$health",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 20.sp,
                                                color = healthColor
                                            )
                                            Text(
                                                text = "/100",
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = healthText,
                                            color = healthColor,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Kiểm tra: " + if(farm.sowingDate != null) "Mới đây" else "Chưa ghi nhận",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                
                                Button(
                                    onClick = onNavigateToScan,
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Quét nhanh", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 3. STEPS / TIMELINE GROWTH TIMELINE CARD
                GrowthProgressCard(farm) {
                    showDatePicker = true
                }

                // 4. ACTION PANEL
                Text(
                    text = "Thao tác quản lý",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Button(
                    onClick = onNavigateToScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("🔍 QUÉT SÂU BỆNH AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToAddHarvest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                ) {
                    Text("📦 GHI NHẬN THU HOẠCH", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                }

                OutlinedButton(
                    onClick = onNavigateToAddTransaction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen)
                ) {
                    Text("💰 THÊM KHOẢN CHI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.updateSowingDate(farm!!, com.google.firebase.Timestamp(Date(millis)))
                    }
                    showDatePicker = false
                }) {
                    Text("XÁC NHẬN", color = ForestGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("HỦY", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun GrowthProgressCard(farm: Farm, onUpdateClick: () -> Unit) {
    val realAgeDays = farm.sowingDate?.let { date ->
        val diffInMillies = System.currentTimeMillis() - date.toDate().time
        java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillies).toInt().coerceAtLeast(0)
    } ?: farm.ageDays

    val totalDays = if (farm.totalGrowthDays > 0) farm.totalGrowthDays else 100
    val progress = (realAgeDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
    val stageName = getStageName(realAgeDays, farm.totalGrowthDays)
    val stageColor = getStageColor(realAgeDays, farm.totalGrowthDays)
    
    val stages = listOf("Mạ non", "Đẻ nhánh", "Làm đòng", "Chín", "Thu hoạch")
    val currentStageIndex = when {
        progress < 0.2f -> 0
        progress < 0.5f -> 1
        progress < 0.8f -> 2
        progress < 1.0f -> 3
        else -> 4
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassCardBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Tiến độ tăng trưởng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = stageColor.copy(alpha = 0.15f),
                        modifier = Modifier.clickable { onUpdateClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Ngày $realAgeDays / $totalDays",
                                color = stageColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = stageColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // -- Biểu Đồ Thanh Ngang (Timeline Stepper) --
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Đường line ngang dính liền (nằm dưới)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until stages.size - 1) {
                                val isLineActive = i < currentStageIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(if (isLineActive) ForestGreen else Color(0xFFEEEEEE))
                                )
                            }
                        }

                        // Các điểm mốc (Nodes nằm trên line ngang)
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
                                        .size(if (isCurrent) 22.dp else 16.dp)
                                        .background(if (isActive) ForestGreen else Color(0xFFEEEEEE), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCurrent) {
                                        Box(modifier = Modifier.size(10.dp).background(Color.White, CircleShape))
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Tên các mốc
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        stages.forEachIndexed { index, name ->
                            val isCurrent = index == currentStageIndex
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                color = if (isCurrent) ForestGreen else Color.Gray,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(55.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
