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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    
    LaunchedEffect(farmsState) {
        if (farmsState is Resource.Success) {
            isInitialized = true
        }
    }
    
    LaunchedEffect(farm, isInitialized) {
        if (isInitialized && farm == null) {
            onNavigateBack()
        }
    }

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
                        var menuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Chỉnh sửa") },
                                    onClick = {
                                        menuExpanded = false
                                        showEditDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Xóa", color = DangerRed) },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteConfirmDialog = true
                                    }
                                )
                            }
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
                // 2. STEPS / TIMELINE GROWTH TIMELINE CARD
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

        // Chỉnh sửa đồng ruộng Dialog
        if (showEditDialog && farm != null) {
            var editName by remember { mutableStateOf(farm.farmName) }
            var editArea by remember { mutableStateOf(farm.areaM2.toString()) }
            var editVariety by remember { mutableStateOf(farm.varietyId) }
            var expandedVarietyDropdown by remember { mutableStateOf(false) }
            val varietiesList = listOf("Lúa ST25", "Jasmine 85", "Đài Thơm 8", "OM5451")

            AlertDialog(
                onDismissRequest = { 
                    showEditDialog = false 
                },
                title = { Text("Chỉnh sửa đồng ruộng", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Tên mảnh ruộng") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ForestGreen)
                        )
                        OutlinedTextField(
                            value = editArea,
                            onValueChange = { editArea = it },
                            label = { Text("Diện tích (m²)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ForestGreen)
                        )
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = expandedVarietyDropdown,
                                onExpandedChange = { expandedVarietyDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = editVariety,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Giống lúa") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVarietyDropdown) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = ForestGreen)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedVarietyDropdown,
                                    onDismissRequest = { expandedVarietyDropdown = false }
                                ) {
                                    varietiesList.forEach { variety ->
                                        DropdownMenuItem(
                                            text = { Text(variety) },
                                            onClick = {
                                                editVariety = variety
                                                expandedVarietyDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val areaVal = editArea.toDoubleOrNull() ?: 0.0
                            if (editName.isNotBlank() && areaVal > 0) {
                                val updatedFarm = farm.copy(
                                    farmName = editName,
                                    areaM2 = areaVal,
                                    varietyId = editVariety
                                )
                                viewModel.updateFarm(updatedFarm)
                                showEditDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text("Lưu lại", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showEditDialog = false 
                        }
                    ) {
                        Text("Hủy", color = TextSecondary)
                    }
                }
            )
        }

        // Xoá đồng ruộng Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteConfirmDialog = false 
                },
                title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = { Text("Bạn có chắc chắn muốn xóa ruộng này? Dữ liệu liên quan cũng sẽ bị ảnh hưởng.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteFarm(fieldId)
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Xóa", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showDeleteConfirmDialog = false 
                        }
                    ) {
                        Text("Hủy", color = TextSecondary)
                    }
                }
            )
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
