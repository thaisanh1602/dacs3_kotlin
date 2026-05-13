package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.ui.theme.GreenPrimary
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.HarvestViewModel
import com.example.angrismart.viewmodel.FieldViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHarvestScreen(
    fieldId: String,
    harvestViewModel: HarvestViewModel = viewModel(),
    fieldViewModel: FieldViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val addHarvestState by harvestViewModel.addHarvestState.collectAsState()

    // --- Form state ---
    var variantName by remember { mutableStateOf("") }
    var totalWeight by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var totalExpense by remember { mutableStateOf("") }
    var cropSeason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val previewRevenue = (totalWeight.toDoubleOrNull() ?: 0.0) * (salePrice.toDoubleOrNull() ?: 0.0)
    val previewProfit = previewRevenue - (totalExpense.toDoubleOrNull() ?: 0.0)

    val vndFormat = remember {
        NumberFormat.getNumberInstance(Locale.Builder().setLanguage("vi").setRegion("VN").build())
    }

    // Chỉ dùng FieldViewModel để tự động điền thông tin giống lúa và vụ mùa
    val farmsState by fieldViewModel.farmsState.collectAsState()

    LaunchedEffect(fieldId) {
        if (fieldId.isNotEmpty()) {
            fieldViewModel.loadFarms()
        }
    }

    // Tự động điền thông tin từ ruộng (chỉ chạy 1 lần khi farm được load)
    LaunchedEffect(farmsState) {
        val farm = (farmsState.data ?: emptyList()).find { it.id == fieldId }
        if (farm != null) {
            if (variantName.isEmpty()) variantName = farm.varietyName

            if (cropSeason.isEmpty()) {
                try {
                    farm.sowingDate?.let { date ->
                        val calendar = java.util.Calendar.getInstance()
                        calendar.time = date.toDate()
                        val month = calendar.get(java.util.Calendar.MONTH) + 1
                        cropSeason = when (month) {
                            in 11..12, 1 -> "Đông Xuân"
                            in 5..8 -> "Hè Thu"
                            in 9..10 -> "Thu Đông"
                            else -> "Mùa Vụ Khác"
                        }
                    } ?: run { cropSeason = "Đông Xuân" }
                } catch (e: Exception) {
                    cropSeason = "Đông Xuân"
                }
            }
        }
    }

    // --- Side effects ---
    LaunchedEffect(addHarvestState) {
        when (val state = addHarvestState) {
            is Resource.Success -> {
                harvestViewModel.resetAddHarvestState()
                onSaveSuccess()
            }
            is Resource.Error -> {
                errorMessage = state.message
                harvestViewModel.resetAddHarvestState()
            }
            else -> {}
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ghi nhận Thu Hoạch",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Preview lợi nhuận (live) ---
            ProfitPreviewCard(
                revenue = previewRevenue,
                expense = totalExpense.toDoubleOrNull() ?: 0.0,
                profit = previewProfit,
                vndFormat = vndFormat
            )

            // --- Form inputs ---
            HarvestFormCard(
                variantName = variantName,
                onVariantNameChange = { variantName = it },
                cropSeason = cropSeason,
                onCropSeasonChange = { cropSeason = it },
                totalWeight = totalWeight,
                onTotalWeightChange = { totalWeight = it },
                salePrice = salePrice,
                onSalePriceChange = { salePrice = it },
                totalExpense = totalExpense,
                onTotalExpenseChange = { totalExpense = it }
            )

            // --- Error message ---
            errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "⚠️ $msg",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFB71C1C),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // --- Nút lưu ---
            val isLoading = addHarvestState is Resource.Loading
            Button(
                onClick = {
                    errorMessage = null
                    harvestViewModel.addHarvest(
                        fieldId = fieldId,
                        variantName = variantName,
                        totalWeightStr = totalWeight,
                        salePriceStr = salePrice,
                        totalExpenseStr = totalExpense,
                        cropSeason = cropSeason
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "💾 LƯU THU HOẠCH",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-components
// ---------------------------------------------------------------------------

@Composable
private fun ProfitPreviewCard(
    revenue: Double,
    expense: Double,
    profit: Double,
    vndFormat: NumberFormat
) {
    val profitColor = if (profit >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
    val profitBg = if (profit >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val gradientColors = if (profit >= 0)
        listOf(Color(0xFF1B5E20), GreenPrimary)
    else
        listOf(Color(0xFFB71C1C), Color(0xFFE53935))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = gradientColors))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "📊 Dự tính lợi nhuận",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PreviewStatItem("Doanh thu", "${vndFormat.format(revenue.toLong())} đ", Color.White)
                    PreviewStatItem("Chi phí", "${vndFormat.format(expense.toLong())} đ", Color.White.copy(alpha = 0.8f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (profit >= 0) "✅ Lợi nhuận" else "❌ Lỗ vốn",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${if (profit >= 0) "+" else ""}${vndFormat.format(profit.toLong())} đ",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewStatItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = color.copy(alpha = 0.8f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HarvestFormCard(
    variantName: String, onVariantNameChange: (String) -> Unit,
    cropSeason: String, onCropSeasonChange: (String) -> Unit,
    totalWeight: String, onTotalWeightChange: (String) -> Unit,
    salePrice: String, onSalePriceChange: (String) -> Unit,
    totalExpense: String, onTotalExpenseChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Thông tin thu hoạch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )

            HarvestTextField(
                value = variantName,
                onValueChange = onVariantNameChange,
                label = "Tên giống lúa",
                placeholder = "VD: Đài Thơm 8",
                leadingEmoji = "🌾"
            )

            HarvestTextField(
                value = cropSeason,
                onValueChange = onCropSeasonChange,
                label = "Tên vụ mùa",
                placeholder = "VD: Đông Xuân 2026",
                leadingEmoji = "📅"
            )

            HarvestTextField(
                value = totalWeight,
                onValueChange = onTotalWeightChange,
                label = "Tổng cân nặng (kg)",
                placeholder = "VD: 3500",
                leadingEmoji = "⚖️",
                keyboardType = KeyboardType.Decimal
            )

            HarvestTextField(
                value = salePrice,
                onValueChange = onSalePriceChange,
                label = "Giá bán (VNĐ/kg)",
                placeholder = "VD: 8500",
                leadingEmoji = "💵",
                keyboardType = KeyboardType.Decimal
            )

            HarvestTextField(
                value = totalExpense,
                onValueChange = onTotalExpenseChange,
                label = "Tổng chi phí (VNĐ)",
                placeholder = "VD: 5000000",
                leadingEmoji = "💸",
                keyboardType = KeyboardType.Decimal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HarvestTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingEmoji: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color.LightGray, fontSize = 14.sp) },
        leadingIcon = { Text(leadingEmoji, fontSize = 20.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenPrimary,
            focusedLabelColor = GreenPrimary
        ),
        singleLine = true
    )
}
