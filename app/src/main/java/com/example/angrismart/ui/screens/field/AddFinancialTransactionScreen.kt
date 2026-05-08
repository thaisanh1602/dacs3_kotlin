package com.example.angrismart.ui.screens.field

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.domain.model.FinancialTransaction
import com.example.angrismart.ui.theme.GreenPrimary
import com.example.angrismart.ui.theme.RedError
import com.example.angrismart.utils.Resource
import com.example.angrismart.viewmodel.FinancialTransactionViewModel
import com.example.angrismart.data.repository.FinancialTransactionRepositoryImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFinancialTransactionScreen(
    fieldId: String,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val repository = remember { FinancialTransactionRepositoryImpl() }
    val viewModel: FinancialTransactionViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return FinancialTransactionViewModel(repository) as T
            }
        }
    )

    var type by remember { mutableStateOf("expense") } // 'expense' or 'income'
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-04-10T15:30:00Z") } // TODO: Use date picker
    var expanded by remember { mutableStateOf(false) }

    val categories = if (type == "expense") listOf("Mua phân bón", "Mua thuốc trừ sâu", "Thuê nhân công", "Thuê máy móc", "Khác") else listOf("Bán lúa", "Trợ cấp", "Khác")

    val addStatus by viewModel.addTransactionStatus.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(addStatus) {
        if (addStatus is Resource.Success) {
            viewModel.resetAddTransactionStatus()
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm Thu/Chi", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Loại giao dịch
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { type = "expense"; category = "" },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "expense") RedError else Color.LightGray,
                        contentColor = if (type == "expense") Color.White else Color.Black
                    )
                ) {
                    Text("Khoản Chi")
                }
                Button(
                    onClick = { type = "income"; category = "" },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "income") GreenPrimary else Color.LightGray,
                        contentColor = if (type == "income") Color.White else Color.Black
                    )
                ) {
                    Text("Khoản Thu")
                }
            }

            // Danh mục
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Danh mục") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = GreenPrimary),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth().height(68.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Số tiền
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Số tiền (VNĐ)") },
                modifier = Modifier.fillMaxWidth().height(68.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ghi chú
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ngày tháng
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Ngày") },
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Lịch", tint = GreenPrimary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary),
                modifier = Modifier.fillMaxWidth().height(68.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (addStatus is Resource.Error) {
                Text(
                    text = addStatus?.message ?: "Lỗi",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    val priceDouble = price.toDoubleOrNull()
                    if (category.isNotBlank() && priceDouble != null) {
                        val transaction = FinancialTransaction(
                            fieldId = fieldId,
                            userUid = "debug_user_123", // TODO: Get from auth
                            type = type,
                            category = category,
                            price = priceDouble,
                            note = note,
                            date = date
                        )
                        viewModel.addTransaction(transaction)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = addStatus !is Resource.Loading
            ) {
                if (addStatus is Resource.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("LƯU", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
