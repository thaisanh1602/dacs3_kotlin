package com.example.angrismart.ui.screens.scan

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

    val riskLabel = when (riskLevel.lowercase()) {
        "high", "cao" -> "Mức độ nguy hiểm cao"
        "medium", "trung bình" -> "Mức độ trung bình"
        else -> "Sức khỏe tốt"
    }

    // Parse confidence percentage (e.g. "87.5%")
    val confidenceValue = confidence.replace("%", "").trim().toFloatOrNull() ?: 80f
    val confidenceFraction = (confidenceValue / 100f).coerceIn(0f, 1f)

    // Animated progress for confidence ring
    var animatedConfidence by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        delay(300)
        animatedConfidence = confidenceFraction
    }
    val animatedRing by animateFloatAsState(
        targetValue = animatedConfidence,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "ring"
    )

    // Entrance animations
    var showHeader by remember { mutableStateOf(false) }
    var showDesc by remember { mutableStateOf(false) }
    var showTreatment by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100); showHeader = true
        delay(200); showDesc = true
        delay(200); showTreatment = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kết quả Chẩn đoán AI",
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Score card ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = showHeader,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 80 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Confidence ring
                        Box(
                            modifier = Modifier.size(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Background track ring
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 10.dp,
                                color = NeutralBg,
                                strokeCap = StrokeCap.Round
                            )
                            // Animated fill ring
                            CircularProgressIndicator(
                                progress = { animatedRing },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 10.dp,
                                color = riskColor,
                                strokeCap = StrokeCap.Round
                            )
                            // Center content
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = riskIcon,
                                    contentDescription = null,
                                    tint = riskColor,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "${confidenceValue.toInt()}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = diseaseName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Độ tin cậy AI: $confidence",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Risk badge pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = riskColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = riskIcon,
                                    contentDescription = null,
                                    tint = riskColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = riskLabel,
                                    color = riskColor,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Symptoms card ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = showDesc,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 80 }
            ) {
                ResultInfoCard(
                    icon = "📋",
                    title = "Triệu chứng",
                    titleColor = InfoBlue,
                    accentColor = InfoBlue,
                    body = description
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Treatment card ────────────────────────────────────────────
            AnimatedVisibility(
                visible = showTreatment,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 80 }
            ) {
                ResultInfoCard(
                    icon = "💊",
                    title = "Hướng dẫn điều trị",
                    titleColor = ForestGreen,
                    accentColor = MintGreen,
                    body = treatment
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Action button ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = showTreatment,
                enter = fadeIn(tween(800))
            ) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(ForestGreen, SageGreen)
                                ),
                                RoundedCornerShape(27.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Quét lại một lá khác",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ResultInfoCard(
    icon: String,
    title: String,
    titleColor: Color,
    accentColor: Color,
    body: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left accent stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
            )
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icon, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = titleColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
