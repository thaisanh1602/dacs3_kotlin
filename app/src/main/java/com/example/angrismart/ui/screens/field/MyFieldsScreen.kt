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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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

    // Search states
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Edit states
    var showEditDialog by remember { mutableStateOf(false) }
    var editingFarm by remember { mutableStateOf<Farm?>(null) }

    // Delete confirmation state
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var farmToDeleteId by remember { mutableStateOf<String?>(null) }

    val displayedFarms = remember(rawFarms, filterNeedsCareOnly, searchQuery) {
        var list = if (filterNeedsCareOnly) {
            rawFarms.filter { getFarmHealth(it) < 60 }
        } else {
            rawFarms
        }
        
        // Filter out harvested fields
        list = list.filter { it.isHarvested == 0 && it.status == "active" }

        if (searchQuery.isNotBlank()) {
            list = list.filter { it.farmName.contains(searchQuery, ignoreCase = true) }
        }
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(GlassBgStart, GlassBgEnd)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (isSearching) {
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Tìm kiếm đồng ruộng...", fontSize = 16.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreen,
                                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.8f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                                ),
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Xóa tìm kiếm", tint = TextPrimary)
                                        }
                                    }
                                }
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSearching = false 
                                searchQuery = "" 
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                            }
                        }
                    )
                } else {
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
                            IconButton(onClick = { isSearching = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = TextPrimary)
                            }
                            IconButton(onClick = onNavigateToAddField) {
                                Icon(Icons.Default.Add, contentDescription = "Thêm ruộng", tint = TextPrimary)
                            }
                        }
                    )
                }
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
                            onClick = { onNavigateToFieldDetail(farm.id) },
                            onEditClick = {
                                editingFarm = farm
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                farmToDeleteId = farm.id
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }

                // Spacing bottom
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Chỉnh sửa đồng ruộng Dialog
            if (showEditDialog && editingFarm != null) {
                var editName by remember { mutableStateOf(editingFarm!!.farmName) }
                var editArea by remember { mutableStateOf(editingFarm!!.areaM2.toString()) }
                var editVariety by remember { mutableStateOf(editingFarm!!.varietyId) }
                var expandedVarietyDropdown by remember { mutableStateOf(false) }
                val varietiesList = listOf("Lúa ST25", "Jasmine 85", "Đài Thơm 8", "OM5451")

                AlertDialog(
                    onDismissRequest = { 
                        showEditDialog = false 
                        editingFarm = null
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
                                    val updatedFarm = editingFarm!!.copy(
                                        farmName = editName,
                                        areaM2 = areaVal,
                                        varietyId = editVariety
                                    )
                                    viewModel.updateFarm(updatedFarm)
                                    showEditDialog = false
                                    editingFarm = null
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
                                editingFarm = null
                            }
                        ) {
                            Text("Hủy", color = TextSecondary)
                        }
                    }
                )
            }

            // Xoá đồng ruộng Dialog
            if (showDeleteConfirmDialog && farmToDeleteId != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showDeleteConfirmDialog = false 
                        farmToDeleteId = null
                    },
                    title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold, color = TextPrimary) },
                    text = { Text("Bạn có chắc chắn muốn xóa ruộng này? Dữ liệu liên quan cũng sẽ bị ảnh hưởng.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteFarm(farmToDeleteId!!)
                                showDeleteConfirmDialog = false
                                farmToDeleteId = null
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
                                farmToDeleteId = null
                            }
                        ) {
                            Text("Hủy", color = TextSecondary)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GridFarmCard(
    farm: Farm,
    variantName: String,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

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

            // 3-dots button for edit/delete
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                var menuExpanded by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tùy chọn ruộng",
                        tint = TextPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Chỉnh sửa") },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Xóa", color = DangerRed) },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        }
                    )
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
