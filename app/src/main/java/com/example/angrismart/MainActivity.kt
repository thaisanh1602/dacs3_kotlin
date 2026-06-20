package com.example.angrismart

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.angrismart.ui.screens.auth.LoginScreen
import com.example.angrismart.ui.screens.field.AddFieldScreen
import com.example.angrismart.ui.screens.field.MyFieldsScreen
import com.example.angrismart.ui.screens.home.HomeDashboardScreen
import com.example.angrismart.ui.screens.scan.ScanDiseaseScreen
import com.example.angrismart.ui.screens.scan.ScanResultScreen
import com.example.angrismart.ui.theme.AngriSmartTheme
import com.example.angrismart.utils.DiseaseCheckWorker
import com.example.angrismart.utils.NotificationHelper
import java.util.concurrent.TimeUnit
import com.example.angrismart.utils.DataSeeder
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Chat
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angrismart.viewmodel.AuthViewModel

enum class Screen { LOGIN, REGISTER, FORGOT_PASSWORD, HOME, MY_FIELDS, ADD_FIELD, SCAN, SCAN_RESULT, FIELD_DETAIL, WEATHER, CHAT, ADD_HARVEST, SEASON_PROFIT, ADD_FINANCIAL_TRANSACTION }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//         DataSeeder.resetData(); // Gỡ bỏ nạp tự động lỗi UID
        // 1. Tạo kênh thông báo Push
        NotificationHelper.createChannel(this)

        // 2. Xin quyền Thông báo (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        // 3. Lập lịch quét bệnh MỖI NGÀY 1 LẦN chạy nền
        val dailyWork = PeriodicWorkRequestBuilder<DiseaseCheckWorker>(
            1, TimeUnit.DAYS // Mỗi 24 giờ
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "disease_daily_check",
            ExistingPeriodicWorkPolicy.KEEP, // Giữ lịch cũ nếu đã có, không tạo trùng
            dailyWork
        )

        setContent {
            AngriSmartTheme {
                val authViewModel: AuthViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
                var selectedFieldId by remember { mutableStateOf("") }
                var harvestBackScreen by remember { mutableStateOf(Screen.FIELD_DETAIL) }

                // Lưu kết quả chẩn đoán AI
                var resultDisease by remember { mutableStateOf("") }
                var resultConfidence by remember { mutableStateOf("") }
                var resultDescription by remember { mutableStateOf("") }
                var resultTreatment by remember { mutableStateOf("") }
                var resultRiskLevel by remember { mutableStateOf("") }
                var resultImagePath by remember { mutableStateOf<String?>(null) }

                // --- Xử lý chặn Nút Lùi Vật Lý (Back Button) của thiết bị ---
                BackHandler(enabled = currentScreen != Screen.LOGIN && currentScreen != Screen.HOME) {
                    currentScreen = when (currentScreen) {
                        Screen.MY_FIELDS -> Screen.HOME
                        Screen.ADD_FIELD -> Screen.MY_FIELDS
                        Screen.FIELD_DETAIL -> Screen.MY_FIELDS
                        Screen.SCAN -> Screen.HOME
                        Screen.SCAN_RESULT -> Screen.SCAN
                        Screen.CHAT -> Screen.HOME
                        Screen.REGISTER -> Screen.LOGIN
                        Screen.ADD_HARVEST -> harvestBackScreen
                        Screen.SEASON_PROFIT -> Screen.HOME
                        Screen.ADD_FINANCIAL_TRANSACTION -> Screen.FIELD_DETAIL
                        Screen.FORGOT_PASSWORD -> Screen.LOGIN
                        else -> currentScreen
                    }
                }

                val isAuthScreen = currentScreen == Screen.LOGIN ||
                        currentScreen == Screen.REGISTER ||
                        currentScreen == Screen.FORGOT_PASSWORD

                if (isAuthScreen) {
                    when (currentScreen) {
                        Screen.LOGIN -> {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = { uid ->
//                                    com.example.angrismart.utils.DataSeeder.seedData(uid)
                                    currentScreen = Screen.HOME
                                },
                                onNavigateToRegister = { currentScreen = Screen.REGISTER },
                                onNavigateToForgotPassword = { currentScreen = Screen.FORGOT_PASSWORD }
                            )
                        }
                        Screen.FORGOT_PASSWORD -> {
                            com.example.angrismart.ui.screens.auth.ForgotPasswordScreen(
                                onNavigateBack = { currentScreen = Screen.LOGIN },
                                authViewModel = authViewModel
                            )
                        }
                        Screen.REGISTER -> {
                            com.example.angrismart.ui.screens.auth.RegisterScreen(
                                viewModel = authViewModel,
                                onNavigateBack = { currentScreen = Screen.LOGIN }
                            )
                        }
                        else -> {}
                    }
                } else {
                    // Màn hình chính có Bottom Navigation
                    val activeTab = when (currentScreen) {
                        Screen.HOME, Screen.SEASON_PROFIT -> Screen.HOME
                        Screen.MY_FIELDS, Screen.FIELD_DETAIL, Screen.ADD_FIELD, Screen.ADD_HARVEST, Screen.ADD_FINANCIAL_TRANSACTION -> Screen.MY_FIELDS
                        Screen.SCAN, Screen.SCAN_RESULT -> Screen.SCAN
                        Screen.WEATHER -> Screen.WEATHER
                        Screen.CHAT -> Screen.CHAT
                        else -> Screen.HOME
                    }

                    Scaffold(
                        bottomBar = {
                            androidx.compose.material3.Surface(
                                color = androidx.compose.ui.graphics.Color(0xE6FFFFFF), // Translucent white tint
                                tonalElevation = 8.dp,
                                modifier = androidx.compose.ui.Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .border(1.dp, androidx.compose.ui.graphics.Color(0x26000000), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                                shadowElevation = 16.dp
                            ) {
                                NavigationBar(
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    tonalElevation = 0.dp,
                                    modifier = androidx.compose.ui.Modifier.windowInsetsPadding(NavigationBarDefaults.windowInsets)
                                ) {
                                    val items = listOf(
                                        Triple(Screen.HOME, "Trang chủ", Icons.Filled.Dashboard),
                                        Triple(Screen.MY_FIELDS, "Đồng ruộng", Icons.Filled.Spa),
                                        Triple(Screen.SCAN, "Quét AI", Icons.Filled.CameraAlt),
                                        Triple(Screen.WEATHER, "Thời tiết", Icons.Filled.WbSunny),
                                        Triple(Screen.CHAT, "Trợ lý AI", Icons.Filled.Chat)
                                    )

                                    items.forEach { item ->
                                        val screen = item.first
                                        val label = item.second
                                        val icon = item.third
                                        val selected = activeTab == screen
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { currentScreen = screen },
                                            icon = {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = if (selected) androidx.compose.ui.graphics.Color(0xFF2D5A27) else androidx.compose.ui.graphics.Color(0xFF6B7B6B),
                                                    modifier = androidx.compose.ui.Modifier.size(24.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = label,
                                                    color = if (selected) androidx.compose.ui.graphics.Color(0xFF2D5A27) else androidx.compose.ui.graphics.Color(0xFF6B7B6B),
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 10.sp
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
                            when (currentScreen) {
                                Screen.HOME -> {
                                    HomeDashboardScreen(
                                        userName = "Bà con",
                                        onNavigateToFields = { currentScreen = Screen.MY_FIELDS },
                                        onNavigateToScan = { currentScreen = Screen.SCAN },
                                        onNavigateToWeather = { currentScreen = Screen.WEATHER },
                                        onNavigateToChat = { currentScreen = Screen.CHAT },
                                        onNavigateToProfit = { currentScreen = Screen.SEASON_PROFIT },
                                        onNavigateToFieldDetail = { id ->
                                            selectedFieldId = id
                                            currentScreen = Screen.FIELD_DETAIL
                                        },
                                        onLogout = {
                                            authViewModel.signOut()
                                            currentScreen = Screen.LOGIN
                                        }
                                    )
                                }
                                Screen.MY_FIELDS -> {
                                    MyFieldsScreen(
                                        onNavigateBack = { currentScreen = Screen.HOME },
                                        onNavigateToAddField = { currentScreen = Screen.ADD_FIELD },
                                        onNavigateToFieldDetail = { id ->
                                            selectedFieldId = id
                                            currentScreen = Screen.FIELD_DETAIL
                                        }
                                    )
                                }
                                Screen.FIELD_DETAIL -> {
                                     com.example.angrismart.ui.screens.field.FieldDetailScreen(
                                         fieldId = selectedFieldId,
                                         onNavigateBack = { currentScreen = Screen.MY_FIELDS },
                                         onNavigateToScan = { currentScreen = Screen.SCAN },
                                         onNavigateToAddHarvest = { 
                                             harvestBackScreen = Screen.FIELD_DETAIL
                                             currentScreen = Screen.ADD_HARVEST 
                                         },
                                         onNavigateToAddTransaction = { currentScreen = Screen.ADD_FINANCIAL_TRANSACTION }
                                     )
                                }
                                Screen.ADD_FIELD -> {
                                    AddFieldScreen(
                                        onNavigateBack = { currentScreen = Screen.MY_FIELDS },
                                        onSaveSuccess = { currentScreen = Screen.MY_FIELDS }
                                    )
                                }
                                Screen.SCAN -> {
                                    ScanDiseaseScreen(
                                        onNavigateBack = { currentScreen = Screen.HOME },
                                        onNavigateToResult = { disease, confidence, description, treatment, riskLevel, imagePath ->
                                            resultDisease = disease
                                            resultConfidence = confidence
                                            resultDescription = description
                                            resultTreatment = treatment
                                            resultRiskLevel = riskLevel
                                            resultImagePath = imagePath
                                            currentScreen = Screen.SCAN_RESULT
                                        }
                                    )
                                }
                                Screen.SCAN_RESULT -> {
                                    ScanResultScreen(
                                        imagePath = resultImagePath,
                                        diseaseName = resultDisease,
                                        confidence = resultConfidence,
                                        description = resultDescription,
                                        treatment = resultTreatment,
                                        riskLevel = resultRiskLevel,
                                        onNavigateBack = { currentScreen = Screen.SCAN }
                                    )
                                }
                                Screen.WEATHER -> {
                                    com.example.angrismart.ui.screens.weather.WeatherScreen(
                                        onNavigateBack = { currentScreen = Screen.HOME }
                                    )
                                }
                                Screen.CHAT -> {
                                    com.example.angrismart.ui.screens.chat.ChatScreen(
                                        onNavigateBack = { currentScreen = Screen.HOME }
                                    )
                                }
                                Screen.ADD_HARVEST -> {
                                     com.example.angrismart.ui.screens.field.AddHarvestScreen(
                                         fieldId = selectedFieldId,
                                         onNavigateBack = { currentScreen = harvestBackScreen },
                                         onSaveSuccess = { currentScreen = Screen.SEASON_PROFIT }
                                     )
                                }
                                Screen.SEASON_PROFIT -> {
                                     com.example.angrismart.ui.screens.field.SeasonProfitScreen(
                                         onNavigateBack = { currentScreen = Screen.HOME },
                                         onNavigateToAddHarvest = { fieldId ->
                                             selectedFieldId = fieldId
                                             harvestBackScreen = Screen.SEASON_PROFIT
                                             currentScreen = Screen.ADD_HARVEST
                                         },
                                         onNavigateToAddField = {
                                             currentScreen = Screen.ADD_FIELD
                                         }
                                     )
                                }
                                Screen.ADD_FINANCIAL_TRANSACTION -> {
                                    com.example.angrismart.ui.screens.field.AddFinancialTransactionScreen(
                                        fieldId = selectedFieldId,
                                        onNavigateBack = { currentScreen = Screen.FIELD_DETAIL },
                                        onSaveSuccess = { currentScreen = Screen.FIELD_DETAIL }
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}