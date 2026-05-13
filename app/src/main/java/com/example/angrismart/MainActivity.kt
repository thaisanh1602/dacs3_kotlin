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
enum class Screen { LOGIN, REGISTER, HOME, MY_FIELDS, ADD_FIELD, SCAN, SCAN_RESULT, FIELD_DETAIL, WEATHER, CHAT, ADD_HARVEST, SEASON_PROFIT, ADD_FINANCIAL_TRANSACTION }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataSeeder.seedData();
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
                var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
                var selectedFieldId by remember { mutableStateOf("") }

                // Lưu kết quả chẩn đoán AI
                var resultDisease by remember { mutableStateOf("") }
                var resultConfidence by remember { mutableStateOf("") }
                var resultDescription by remember { mutableStateOf("") }
                var resultTreatment by remember { mutableStateOf("") }
                var resultRiskLevel by remember { mutableStateOf("") }

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
                        Screen.ADD_HARVEST -> Screen.FIELD_DETAIL
                        Screen.SEASON_PROFIT -> Screen.HOME
                        Screen.ADD_FINANCIAL_TRANSACTION -> Screen.FIELD_DETAIL
                        else -> currentScreen
                    }
                }

                when (currentScreen) {
                    Screen.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = { currentScreen = Screen.HOME },
                            onNavigateToRegister = { currentScreen = Screen.REGISTER }
                        )
                    }
                    Screen.REGISTER -> {
                        com.example.angrismart.ui.screens.auth.RegisterScreen(
                            onNavigateBack = { currentScreen = Screen.LOGIN }
                        )
                    }
                    Screen.HOME -> {
                        HomeDashboardScreen(
                            userName = "Bà con",
                            onNavigateToFields = { currentScreen = Screen.MY_FIELDS },
                            onNavigateToScan = { currentScreen = Screen.SCAN },
                            onNavigateToWeather = { currentScreen = Screen.WEATHER },
                            onNavigateToChat = { currentScreen = Screen.CHAT },
                            onNavigateToProfit = { currentScreen = Screen.SEASON_PROFIT }
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
                            onNavigateToAddHarvest = { currentScreen = Screen.ADD_HARVEST },
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
                            onNavigateToResult = { disease, confidence, description, treatment, riskLevel ->
                                resultDisease = disease
                                resultConfidence = confidence
                                resultDescription = description
                                resultTreatment = treatment
                                resultRiskLevel = riskLevel
                                currentScreen = Screen.SCAN_RESULT
                            }
                        )
                    }
                    Screen.SCAN_RESULT -> {
                        ScanResultScreen(
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
                            onNavigateBack = { currentScreen = Screen.FIELD_DETAIL },
                            onSaveSuccess = { currentScreen = Screen.SEASON_PROFIT }
                        )
                    }
                    Screen.SEASON_PROFIT -> {
                        com.example.angrismart.ui.screens.field.SeasonProfitScreen(
                            onNavigateBack = { currentScreen = Screen.HOME },
                            onNavigateToAddHarvest = { /* Có thể điều hướng đến màn hình chọn ruộng trước */ }
                        )
                    }
                    Screen.ADD_FINANCIAL_TRANSACTION -> {
                        com.example.angrismart.ui.screens.field.AddFinancialTransactionScreen(
                            fieldId = selectedFieldId,
                            onNavigateBack = { currentScreen = Screen.FIELD_DETAIL },
                            onSaveSuccess = { currentScreen = Screen.FIELD_DETAIL }
                        )
                    }
                }
            }
        }
    }
}