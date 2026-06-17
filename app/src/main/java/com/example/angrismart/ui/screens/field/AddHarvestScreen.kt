package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.ui.theme.*
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.HarvestViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHarvestScreen(
    fieldId: String,
    harvestViewModel: HarvestViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val addHarvestState by harvestViewModel.addHarvestState.collectAsState()

    // --- Form state variables ---
    var variantName by remember { mutableStateOf("") }
    var totalWeight by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var totalExpense by remember { mutableStateOf("") }
    var cropSeason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Live preview variables
    val previewRevenue = (totalWeight.toDoubleOrNull() ?: 0.0) * (salePrice.toDoubleOrNull() ?: 0.0)
    val previewProfit = previewRevenue - (totalExpense.toDoubleOrNull() ?: 0.0)
    val vndFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    // Handle firebase write callbacks
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
                        text = "Ghi nhận Thu Hoạch",
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
        containerColor = NeutralBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live profit estimator preview
            ProfitPreviewCard(
                revenue = previewRevenue,
                expense = totalExpense.toDoubleOrNull() ?: 0.0,
                profit = previewProfit,
                vndFormat = vndFormat
            )

            // Input form fields card
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

            // Error display if failed
            errorMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "⚠️ $msg",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Save Submit button
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
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0F522E), GreenPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "💾 LƯU THU HOẠCH",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfitPreviewCard(
    revenue: Double,
    expense: Double,
    profit: Double,
    vndFormat: NumberFormat
) {
    val isProfit = profit >= 0
    val gradientColors = if (isProfit)
        listOf(Color(0xFF0D5C34), GreenPrimary)
    else
        listOf(Color(0xFFB71C1C), Color(0xFFE53935))

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
                Text(
                    text = "📊 Dự tính lợi nhuận",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PreviewStatItem("Doanh thu", "${vndFormat.format(revenue.toLong())} đ", Color.White)
                    PreviewStatItem("Chi phí", "${vndFormat.format(expense.toLong())} đ", Color.White.copy(alpha = 0.8f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isProfit) "✅ Lợi nhuận" else "❌ Lỗ vốn",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${if (isProfit) "+" else ""}${vndFormat.format(profit.toLong())} đ",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 26.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewStatItem(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = color.copy(alpha = 0.7f)
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Black,
                color = color
            )
        )
    }
}

@Composable
private fun HarvestFormCard(
    variantName: String, onVariantNameChange: (String) -> Unit,
    cropSeason: String, onCropSeasonChange: (String) -> Unit,
    totalWeight: String, onTotalWeightChange: (String) -> Unit,
    salePrice: String, onSalePriceChange: (String) -> Unit,
    totalExpense: String, onTotalExpenseChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Thông tin thu hoạch",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D3321)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(4.dp)
                    .background(GreenPrimary, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))

            HarvestTextField(
                value = variantName,
                onValueChange = onVariantNameChange,
                label = "Tên giống lúa",
                placeholder = "Ví dụ: Đài Thơm 8",
                leadingEmoji = "🌾"
            )

            HarvestTextField(
                value = cropSeason,
                onValueChange = onCropSeasonChange,
                label = "Tên vụ mùa",
                placeholder = "Ví dụ: Đông Xuân 2026",
                leadingEmoji = "📅"
            )

            HarvestTextField(
                value = totalWeight,
                onValueChange = onTotalWeightChange,
                label = "Tổng cân nặng (kg)",
                placeholder = "Ví dụ: 3500",
                leadingEmoji = "⚖️",
                keyboardType = KeyboardType.Decimal
            )

            HarvestTextField(
                value = salePrice,
                onValueChange = onSalePriceChange,
                label = "Giá bán (VNĐ/kg)",
                placeholder = "Ví dụ: 8500",
                leadingEmoji = "💵",
                keyboardType = KeyboardType.Decimal
            )

            HarvestTextField(
                value = totalExpense,
                onValueChange = onTotalExpenseChange,
                label = "Tổng chi phí (VNĐ)",
                placeholder = "Ví dụ: 5000000",
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
        placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp) },
        leadingIcon = {
            Box(
                modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(leadingEmoji, fontSize = 18.sp)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = Color(0xFFCFD8DC),
            focusedLabelColor = GreenPrimary
        ),
        singleLine = true
    )
}
