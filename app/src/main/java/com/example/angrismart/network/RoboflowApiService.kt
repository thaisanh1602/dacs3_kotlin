package com.example.angrismart.network

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Gọi Roboflow Serverless Inference API bằng OkHttp thuần.
 *
 * Model rice-leaf-wfax3/2 là CLASSIFICATION model → response trả về:
 *   - "top"        : tên class có confidence cao nhất
 *   - "confidence" : confidence của top class (0.0 – 1.0)
 *   - "predictions": array tất cả class (có thể rỗng nếu dưới threshold)
 *
 * Kotlin/OkHttp tương đương với JavaScript/axios sau:
 *   axios({
 *     method: "POST",
 *     url: "https://serverless.roboflow.com/rice-leaf-wfax3/2",
 *     params: { api_key: "pXB6PH9xrIvBq48CQB9X", confidence: 10 },
 *     data: base64Image,
 *     headers: { "Content-Type": "application/x-www-form-urlencoded" }
 *   })
 */
object RoboflowApiService {

    private const val TAG         = "RoboflowApiService"
    private const val API_KEY     = "pXB6PH9xrIvBq48CQB9X"
    private const val MODEL_URL   = "https://serverless.roboflow.com/rice-leaf-disease-classification-ekzer/1"

    // Roboflow mặc định filter predictions dưới 40% → đặt về 10% để không bỏ sót bệnh
    private const val CONFIDENCE  = 10

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Phân tích ảnh và trả về danh sách Prediction (đã sort theo confidence giảm dần).
     * @throws HttpStatusException nếu server trả mã lỗi HTTP
     * @throws java.net.SocketTimeoutException nếu timeout
     * @throws java.io.IOException nếu lỗi mạng
     */
    suspend fun detectDisease(imageFile: File): List<Prediction> {
        return withContext(Dispatchers.IO) {

            // 1. Đọc file ảnh và encode sang Base64 (NO_WRAP = không xuống dòng)
            val base64Image = Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)
            Log.d(TAG, "Gửi ảnh ${imageFile.name} (${imageFile.length() / 1024}KB)")

            // 2. Body là raw base64 string, Content-Type = application/x-www-form-urlencoded
            val requestBody = base64Image
                .toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())

            // 3. Xây dựng request — api_key + confidence là query params
            val request = Request.Builder()
                .url("$MODEL_URL?api_key=$API_KEY&confidence=$CONFIDENCE")
                .post(requestBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            // 4. Thực thi request
            val response     = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "HTTP ${response.code}")

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.e(TAG, "Lỗi HTTP ${response.code}: $responseBody")
                throw HttpStatusException(response.code, "HTTP ${response.code}")
            }

            // Log toàn bộ response để debug
            Log.d(TAG, "=== FULL RESPONSE ===\n$responseBody")

            // 5. Parse JSON response → List<Prediction>
            parseResponse(responseBody)
        }
    }

    /**
     * Parse linh hoạt cả 2 format của Roboflow:
     *
     * [FORMAT A - Classification model]:
     * {
     *   "top": "brown_spot",
     *   "confidence": 0.72,
     *   "predictions": [
     *     { "class": "brown_spot", "class_id": 0, "confidence": 0.72 },
     *     { "class": "healthy",    "class_id": 1, "confidence": 0.18 }
     *   ]
     * }
     *
     * [FORMAT B - Object Detection model]:
     * {
     *   "predictions": [
     *     { "x": 100, "y": 120, "width": 80, "height": 60,
     *       "confidence": 0.92, "class": "brown_spot" }
     *   ]
     * }
     */
    private fun parseResponse(json: String): List<Prediction> {
        return try {
            val root = JSONObject(json)

            // --- Thử FORMAT A (Classification): dùng field "top" + "confidence" ---
            val topClass      = root.optString("top", "")
            val topConfidence = root.optDouble("confidence", -1.0)

            if (topClass.isNotEmpty() && topConfidence >= 0) {
                Log.d(TAG, "Classification result → top='$topClass', confidence=$topConfidence")
                // Trả về 1 prediction duy nhất từ field top/confidence
                return listOf(
                    Prediction(
                        x          = 0.0,
                        y          = 0.0,
                        width      = 0.0,
                        height     = 0.0,
                        confidence = topConfidence,
                        className  = topClass
                    )
                )
            }

            // --- Thử FORMAT B (Detection): dùng array "predictions" ---
            val predsArray = root.optJSONArray("predictions")
            if (predsArray == null || predsArray.length() == 0) {
                Log.w(TAG, "Không tìm thấy predictions trong response")
                return emptyList()
            }

            val result = mutableListOf<Prediction>()
            for (i in 0 until predsArray.length()) {
                val p = predsArray.getJSONObject(i)
                val className = p.optString("class", "").ifEmpty {
                    p.optString("class_name", "")
                }
                val confidence = p.optDouble("confidence", 0.0)
                if (className.isNotEmpty()) {
                    result.add(
                        Prediction(
                            x          = p.optDouble("x", 0.0),
                            y          = p.optDouble("y", 0.0),
                            width      = p.optDouble("width", 0.0),
                            height     = p.optDouble("height", 0.0),
                            confidence = confidence,
                            className  = className
                        )
                    )
                }
            }

            // Sort theo confidence giảm dần
            result.sortByDescending { it.confidence }
            Log.d(TAG, "Detection result: ${result.size} predictions parsed")
            result

        } catch (e: Exception) {
            Log.e(TAG, "Parse lỗi: ${e.message}\nJSON: $json")
            emptyList()
        }
    }
}
