package com.example.angrismart.ui.screens.scan

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
import com.example.angrismart.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    diseaseName: String,
    confidence: String,
    description: String,
    treatment: String,
    riskLevel: String,
    onNavigateBack: () -> Unit
) {
    val riskColor = when (riskLevel.lowercase()) {
        "high", "cao" -> DangerRed
        "medium", "trung bình" -> WarningAmber
        else -> ForestGreen
    }
    
    val riskIcon = when (riskLevel.lowercase()) {
        "high", "cao" -> Icons.Default.Warning
        "medium", "trung bình" -> Icons.Default.Info
        else -> Icons.Default.CheckCircle
    }

    // Dynamic health score based on risk and confidence
    val score = remember(riskLevel, confidence) {
        val confVal = confidence.replace("%", "").trim().toDoubleOrNull()?.toInt() ?: 80
        val baseScore = when (riskLevel.lowercase()) {
            "high", "cao" -> 40
            "medium", "trung bình" -> 68
            else -> 88
        }
        (baseScore + (confVal % 11) - 5).coerceIn(10, 100)
    }

    val scoreText = when {
        score < 55 -> "Cần lưu ý"
        score < 75 -> "Khá ổn"
        else -> "Khỏe mạnh"
    }

    // Animation States
    var showHeader by remember { mutableStateOf(false) }
    var showDesc by remember { mutableStateOf(false) }
    var showTreatment by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showHeader = true
        delay(200)
        showDesc = true
        delay(200)
        showTreatment = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(GlassBgStart, GlassBgEnd)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Kết quả chẩn đoán", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở lại", tint = TextPrimary)
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. PLANT HEALTH ASSESSMENT CARD (Score Circular progress ring)
                AnimatedVisibility(
                    visible = showHeader,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500), initialOffsetY = { 100 })
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassCardBg),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GlassCardBorder, RoundedCornerShape(24.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(riskColor.copy(alpha = 0.05f), Color.White.copy(alpha = 0.8f))
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Side: Circular score ring
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(86.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = { score.toFloat() / 100f },
                                        modifier = Modifier.fillMaxSize(),
                                        color = riskColor,
                                        strokeWidth = 7.dp,
                                        trackColor = Color(0xFFEEEEEE)
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$score",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp,
                                            color = riskColor
                                        )
                                        Text(
                                            text = "/100",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = scoreText,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = riskColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Right Side: Assessment Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LightMint)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Sức khỏe cây lúa",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = diseaseName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Progress bar showing confidence
                                    val confVal = confidence.replace("%", "").trim().toFloatOrNull() ?: 80f
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LinearProgressIndicator(
                                            progress = { confVal / 100f },
                                            color = riskColor,
                                            trackColor = Color(0xFFEEEEEE),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$confidence",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. ANALYSIS SUMMARY CARD
                AnimatedVisibility(
                    visible = showDesc,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500), initialOffsetY = { 100 })
                ) {
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📋", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Tóm tắt phân tích",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    // Action Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(WarningAmber.copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Yêu cầu xử lý",
                                            color = WarningAmber,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Chips for diagnosis tags
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val chipText1 = if (riskLevel.lowercase() == "high" || riskLevel.lowercase() == "cao") "Mức độ nguy cấp" else "Theo dõi thường xuyên"
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(chipText1, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = riskColor) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = riskColor.copy(alpha = 0.1f))
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Chẩn đoán AI", fontSize = 11.sp, color = TextSecondary) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.LightGray.copy(alpha = 0.15f))
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. TREATMENT GUIDANCE CARD
                AnimatedVisibility(
                    visible = showTreatment,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500), initialOffsetY = { 100 })
                ) {
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💊", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hướng dẫn điều trị",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = treatment,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                // 4. ACTION BUTTONS
                AnimatedVisibility(
                    visible = showTreatment,
                    enter = fadeIn(tween(800))
                ) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text(
                            "QUÉT LẠI MỘT LÁ KHÁC", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
