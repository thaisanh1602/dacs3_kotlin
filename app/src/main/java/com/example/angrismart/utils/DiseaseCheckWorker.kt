package com.example.angrismart.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.angrismart.data.remote.WeatherRetrofitClient
import com.example.angrismart.domain.model.Farm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Worker chạy nền MỖI NGÀY 1 LẦN:
 * 1. Lọc ra danh sách cụ thể từng cánh đồng do người dùng này quản lý
 * 2. Gọi API thời tiết 7 ngày tới
 * 3. Chạy AI kiểm tra rủi ro (DiseaseRiskChecker)
 * 4. Phát Push Notification cảnh báo an toàn Hoặc nguy cơ bệnh
 */
class DiseaseCheckWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Lấy ID người dùng hiện tại đang đăng nhập
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Log.d("DiseaseCheckWorker", "Khách chưa đăng nhập -> Tạm dừng quét.")
                return Result.success() // Quả nhiên chưa Login thì không có ruộng
            }

            // Gọi DB lấy mảng Cánh đồng
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("farms")
                .whereEqualTo("userId", userId)
                .get().await()
                
            val farms = snapshot.documents.mapNotNull { it.toObject(Farm::class.java) }

            // Người dùng chưa khai báo ruộng nào -> Không cần báo Push
            if (farms.isEmpty()) {
                Log.d("DiseaseCheckWorker", "Chưa có cánh đồng nào để quét.")
                return Result.success()
            }

            // TODO: Ở tương lai có tọa độ GPS từng ruộng sẽ gọi lặp n lần tuỳ khu vực
            // Hiện tại xài Tọa độ của TP.HCM làm Tâm theo dõi thời tiết 7 ngày
            val response = WeatherRetrofitClient.weatherService.getForecast(
                lat = 10.762622,
                lon = 106.660172
            )

            val daily = response.daily
            val riskResult = DiseaseRiskChecker.checkRisk(
                dates = daily.time,
                tempMax = daily.tempMax,
                tempMin = daily.tempMin,
                humidityMax = daily.humidityMax
            )
            
            val farmNamesStr = farms.joinToString(", ") { it.farmName }

            if (riskResult.hasRisk) {
                // Có nguy cơ => BÁO CÁO CÁC MẢNH RUỘNG NGUY HIỂM
                NotificationHelper.showDiseaseAlert(
                    context = appContext,
                    title = "⚠️ Báo động: Rủi ro Dịch Bệnh Phơi Nhiễm",
                    body = "Ruộng nằm trong diện có nguy cơ: [${farmNamesStr}]\nDự kiến ngày bùng phát: ${riskResult.riskDay}\nNguyên nhân: Thời tiết độ ẩm cao sinh ra ${riskResult.diseaseName}. ${riskResult.reason}"
                )
            } else {
                // Không có nguy cơ => BÁO CÁO RUỘNG ĐANG AN TOÀN TOÀN TẬP
                NotificationHelper.showDiseaseAlert(
                    context = appContext,
                    title = "✅ Bản tin Sức khoẻ Nông vụ: An Toàn",
                    body = "Xin chúc mừng! Toàn bộ ${farms.size} ruộng của bạn gồm: [${farmNamesStr}] hiện đang phát triển ổn định, không ghi nhận đe doạ từ thời tiết trong tuần tới."
                )
            }

            Log.d("DiseaseCheckWorker", "Hoàn tất quét nền cho ${farms.size} ruộng.")
            Result.success()
        } catch (e: Exception) {
            Log.e("DiseaseCheckWorker", "Lỗi rớt mạng hoặc hệ thống khi quét nền", e)
            Result.retry() // Xin hệ thống Android lát nữa cấp slot chạy bù
        }
    }
}
