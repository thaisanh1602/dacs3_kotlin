package com.example.angrismart.ui.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.angrismart.ui.theme.GreenPrimary
import com.example.angrismart.ui.theme.RedError
import com.example.angrismart.ui.theme.YellowWarning

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
        "high" -> RedError
        "medium" -> YellowWarning
        else -> GreenPrimary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kết quả Chẩn đoán AI", color = Color.White, fontWeight = FontWeight.Bold) },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thẻ Tên bệnh + Mức độ tin cậy    
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = diseaseName,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                        color = riskColor,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Độ tin cậy: $confidence",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Nhãn mức độ rủi ro
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = riskColor
                    ) {
                        Text(
                            text = when (riskLevel.lowercase()) {
                                "high" -> "⚠️ MỨC ĐỘ NGUY HIỂM CAO"
                                "medium" -> "⚡ MỨC ĐỘ TRUNG BÌNH"
                                else -> "✅ MỨC ĐỘ THẤP"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mô tả triệu chứng
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "📋 Mô tả triệu chứng",
                        style = MaterialTheme.typography.titleMedium,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hướng dẫn điều trị
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "💊 Hướng dẫn điều trị",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = treatment,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút quét lại
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("QUÉT LẠI MỘT LÁ KHÁC", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
