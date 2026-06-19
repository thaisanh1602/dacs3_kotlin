package com.example.angrismart.network

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Gọi Roboflow Serverless Workflow API.
 * Workflow: workspace="nguyen-van-chau", id="general-segmentation-api"
 * Classes: healthy, bacterial_leaf_blight, brown_spot
 */
object RoboflowWorkflowService {

    private const val TAG = "RoboflowWorkflow"

    private const val API_KEY     = "pXB6PH9xrIvBq48CQB9X"
    private const val WORKSPACE   = "nguyen-van-chau"
    private const val WORKFLOW_ID = "general-segmentation-api"
    private const val CLASSES     = "healthy, bacterial_leaf_blight, brown_spot"
    private const val BASE_URL    = "https://serverless.roboflow.com"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)   // Serverless cold-start có thể chậm hơn
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Gửi ảnh lên workflow và trả về danh sách predictions.
     * @throws HttpStatusException nếu server trả mã lỗi HTTP
     * @throws java.net.SocketTimeoutException nếu timeout
     * @throws java.io.IOException nếu lỗi mạng
     */
    suspend fun detectDisease(imageFile: File): List<WorkflowPrediction> {
        return withContext(Dispatchers.IO) {
            // Chuyển ảnh sang base64
            val imageBytes = imageFile.readBytes()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            Log.d(TAG, "Gửi ảnh ${imageFile.name} (${imageBytes.size / 1024}KB) lên workflow")

            // Build JSON request theo Roboflow Inference SDK format
            val requestJson = JSONObject().apply {
                put("api_key", API_KEY)
                put("inputs", JSONObject().apply {
                    put("image", JSONObject().apply {
                        put("type", "base64")
                        put("value", base64Image)
                    })
                    put("classes", CLASSES)
                })
            }

            val requestBody = requestJson.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("$BASE_URL/$WORKSPACE/workflows/$WORKFLOW_ID")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Workflow HTTP ${response.code}")

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.e(TAG, "Lỗi HTTP ${response.code}: $responseBody")
                throw HttpStatusException(response.code, "HTTP ${response.code}")
            }

            Log.d(TAG, "Workflow response: ${responseBody.take(500)}")
            parseWorkflowResponse(responseBody)
        }
    }

    /**
     * Parse response linh hoạt — thử nhiều path JSON khác nhau tùy workflow config.
     *
     * Roboflow workflow thường trả về 1 trong 2 format:
     *  Format A (outputs array):
     *    {"outputs": [{"predictions": {"predictions": [...]}}]}
     *  Format B (outputs map):
     *    {"outputs": [{"<output_key>": {"predictions": [...]}}]}
     */
    private fun parseWorkflowResponse(json: String): List<WorkflowPrediction> {
        return try {
            val root = JSONObject(json)
            val predictions = mutableListOf<WorkflowPrediction>()

            // --- Thử Format A / B qua "outputs" array ---
            if (root.has("outputs")) {
                val outputs = root.getJSONArray("outputs")
                if (outputs.length() > 0) {
                    val firstOutput = outputs.getJSONObject(0)
                    extractPredictions(firstOutput, predictions)
                }
            }

            // --- Thử root trực tiếp nếu outputs rỗng ---
            if (predictions.isEmpty()) {
                extractPredictions(root, predictions)
            }

            Log.d(TAG, "Parsed ${predictions.size} predictions")
            predictions
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi parse response: ${e.message}")
            emptyList()
        }
    }

    /**
     * Tìm mảng predictions trong một JSONObject (bất kể tên key cụ thể).
     */
    private fun extractPredictions(obj: JSONObject, out: MutableList<WorkflowPrediction>) {
        // Thử path trực tiếp: obj.predictions.predictions[]
        tryExtractFromPredictionsBlock(obj, out)
        if (out.isNotEmpty()) return

        // Thử tất cả key trong object (workflow output key có thể là tên bất kỳ)
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val child = obj.optJSONObject(key) ?: continue
            tryExtractFromPredictionsBlock(child, out)
            if (out.isNotEmpty()) return
        }
    }

    private fun tryExtractFromPredictionsBlock(block: JSONObject, out: MutableList<WorkflowPrediction>) {
        // Format: block.predictions (array hoặc object chứa array)
        val predArray: JSONArray? = block.optJSONArray("predictions")
            ?: block.optJSONObject("predictions")?.optJSONArray("predictions")

        predArray?.let { arr ->
            for (i in 0 until arr.length()) {
                val p = arr.optJSONObject(i) ?: continue
                val className  = p.optString("class", "").ifEmpty { p.optString("class_name", "") }
                val confidence = p.optDouble("confidence", 0.0)
                if (className.isNotEmpty()) {
                    out.add(WorkflowPrediction(className, confidence))
                }
            }
        }

        // Format: block.top (zero-shot classification result)
        if (out.isEmpty() && block.has("top")) {
            val topClass      = block.optString("top", "")
            val topConfidence = block.optDouble("confidence", 0.0)
            if (topClass.isNotEmpty()) {
                out.add(WorkflowPrediction(topClass, topConfidence))
            }
        }
    }
}
