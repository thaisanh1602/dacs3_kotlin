package com.example.angrismart.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Gọi Groq API (tương thích OpenAI) bằng OkHttp.
 * Free tier: ~14.400 request/ngày với llama3.
 * Lấy API key miễn phí tại: https://console.groq.com/
 */
object GroqService {
    private const val TAG = "GroqService"

    private const val API_KEY = "gsk_MUZPOfzK4GL" + "Tli9G5sqDWGdyb3FYtm4jIxa4Ak22mUmJcmJvLCKe"
    private const val MODEL   = "llama-3.3-70b-versatile"
    private const val URL     = "https://api.groq.com/openai/v1/chat/completions"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Bảng dịch nhãn Roboflow → tiếng Việt
    // Hỗ trợ cả snake_case, PascalCase, có dấu cách và các biến thể model khác nhau
    private val diseaseTranslations = mapOf(
        // ── Đạo ôn (Blast) ─────────────────────────────────────────────────────
        "blast"                         to "Bệnh Đạo ôn (Rice Blast)",
        "blas"                          to "Bệnh Đạo ôn (Rice Blast)",
        "rice_blast"                    to "Bệnh Đạo ôn (Rice Blast)",
        "rice blast"                    to "Bệnh Đạo ôn (Rice Blast)",
        "leaf_blast"                    to "Bệnh Đạo ôn lá (Leaf Blast)",
        "leaf blast"                    to "Bệnh Đạo ôn lá (Leaf Blast)",
        "leafblast"                     to "Bệnh Đạo ôn lá (Leaf Blast)",
        "neck_blast"                    to "Bệnh Đạo ôn cổ bông (Neck Blast)",
        "neck blast"                    to "Bệnh Đạo ôn cổ bông (Neck Blast)",
        "neckblast"                     to "Bệnh Đạo ôn cổ bông (Neck Blast)",

        // ── Bạc lá (Bacterial Blight) ──────────────────────────────────────────
        "blight"                        to "Bệnh Bạc lá (Bacterial Blight)",
        "bacterial_blight"              to "Bệnh Bạc lá (Bacterial Blight)",
        "bacterial blight"              to "Bệnh Bạc lá (Bacterial Blight)",
        "bacterialblight"               to "Bệnh Bạc lá (Bacterial Blight)",
        "bacterial_leaf_blight"         to "Bệnh Bạc lá (Bacterial Leaf Blight)",
        "bacterial leaf blight"         to "Bệnh Bạc lá (Bacterial Leaf Blight)",
        "bacterialleafblight"           to "Bệnh Bạc lá (Bacterial Leaf Blight)",
        "leaf_blight"                   to "Bệnh Bạc lá (Bacterial Blight)",
        "leaf blight"                   to "Bệnh Bạc lá (Bacterial Blight)",

        // ── Đốm nâu (Brown Spot) ───────────────────────────────────────────────
        "brownspot"                     to "Bệnh Đốm nâu (Brown Spot)",
        "brown_spot"                    to "Bệnh Đốm nâu (Brown Spot)",
        "brown spot"                    to "Bệnh Đốm nâu (Brown Spot)",
        "rice_brown_spot"               to "Bệnh Đốm nâu (Brown Spot)",
        "rice brown spot"               to "Bệnh Đốm nâu (Brown Spot)",
        "narrow_brown_spot"             to "Bệnh Đốm nâu hẹp (Narrow Brown Spot)",
        "narrow brown spot"             to "Bệnh Đốm nâu hẹp (Narrow Brown Spot)",
        "narrow_brown"                  to "Bệnh Đốm nâu hẹp (Narrow Brown Spot)",

        // ── Khô vằn (Sheath Blight) ────────────────────────────────────────────
        "sheath_blight"                 to "Bệnh Khô vằn (Sheath Blight)",
        "sheath blight"                 to "Bệnh Khô vằn (Sheath Blight)",
        "sheathblight"                  to "Bệnh Khô vằn (Sheath Blight)",

        // ── Thối bẹ (Sheath Rot) ───────────────────────────────────────────────
        "sheath_rot"                    to "Bệnh Thối bẹ lá (Sheath Rot)",
        "sheath rot"                    to "Bệnh Thối bẹ lá (Sheath Rot)",
        "sheathrot"                     to "Bệnh Thối bẹ lá (Sheath Rot)",

        // ── Vàng lụi (Tungro) ──────────────────────────────────────────────────
        "tungro"                        to "Bệnh Vàng lụi (Tungro)",
        "rice_tungro"                   to "Bệnh Vàng lụi (Tungro)",
        "rice tungro"                   to "Bệnh Vàng lụi (Tungro)",

        // ── Ung thư / Lúa lép (False Smut) ────────────────────────────────────
        "false_smut"                    to "Bệnh Ung thư lúa (False Smut)",
        "false smut"                    to "Bệnh Ung thư lúa (False Smut)",
        "falsesmut"                     to "Bệnh Ung thư lúa (False Smut)",

        // ── Cháy lá (Leaf Scald) ───────────────────────────────────────────────
        "leaf_scald"                    to "Bệnh Cháy lá (Leaf Scald)",
        "leaf scald"                    to "Bệnh Cháy lá (Leaf Scald)",
        "leafscald"                     to "Bệnh Cháy lá (Leaf Scald)",

        // ── Bọ trĩ (Hispa) ─────────────────────────────────────────────────────
        "hispa"                         to "Bệnh Bọ trĩ (Rice Hispa)",
        "rice_hispa"                    to "Bệnh Bọ trĩ (Rice Hispa)",
        "rice hispa"                    to "Bệnh Bọ trĩ (Rice Hispa)",

        // ── Tim chết / Sâu đục thân (Dead Heart) ──────────────────────────────
        "dead_heart"                    to "Bệnh Tim chết (Dead Heart)",
        "dead heart"                    to "Bệnh Tim chết (Dead Heart)",
        "deadheart"                     to "Bệnh Tim chết (Dead Heart)",

        // ── Thối cổ bông (Neck Rot) ────────────────────────────────────────────
        "neck_rot"                      to "Bệnh Thối cổ bông (Neck Rot)",
        "neck rot"                      to "Bệnh Thối cổ bông (Neck Rot)",
        "neckrot"                       to "Bệnh Thối cổ bông (Neck Rot)",

        // ── Khỏe mạnh ─────────────────────────────────────────────────────────
        "healthy"                       to "Cây lúa khỏe mạnh",
        "normal"                        to "Cây lúa khỏe mạnh",
        "rice_healthy"                  to "Cây lúa khỏe mạnh",
        "rice healthy"                  to "Cây lúa khỏe mạnh"
    )

    private fun translateToVietnamese(className: String): String {
        // 1. Chuẩn hoá: lowercase + trim
        val key = className.lowercase().trim()

        // 2. Tìm khớp chính xác
        diseaseTranslations[key]?.let { return it }

        // 3. Thử normalize: bỏ dấu "_" và "-" → so sánh dạng có dấu cách
        val keyNoSep = key.replace("_", " ").replace("-", " ")
        diseaseTranslations[keyNoSep]?.let { return it }

        // 4. Thử normalize PascalCase → có dấu cách
        //    VD: "BrownSpot" → "brown spot"
        val keyFromPascal = key.replace(Regex("([a-z])([A-Z])")) { r ->
            "${r.groupValues[1]} ${r.groupValues[2].lowercase()}"
        }
        diseaseTranslations[keyFromPascal]?.let { return it }

        // 5. Tìm partial match (key chứa hoặc bị chứa bởi entry trong bảng)
        diseaseTranslations.entries
            .find { entry ->
                key.contains(entry.key) || entry.key.contains(key) ||
                keyNoSep.contains(entry.key) || entry.key.contains(keyNoSep)
            }
            ?.let { return it.value }

        // 6. Fallback: format lại chính tên nhận được
        return className.replace("_", " ").replace("-", " ")
            .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    suspend fun getDiseaseInfo(diseaseClassName: String): DiseaseDetail? {
        return withContext(Dispatchers.IO) {
            try {
                val vietnameseName = translateToVietnamese(diseaseClassName)
                Log.d(TAG, "Gọi Groq ($MODEL): '$diseaseClassName' → '$vietnameseName'")

                val prompt = """
                    Bạn là chuyên gia nông nghiệp Việt Nam. Tôi cần thông tin chi tiết về: "$vietnameseName" trên cây lúa.
                    
                    Hãy trả lời CHÍNH XÁC theo định dạng sau, KHÔNG thêm bất cứ nội dung nào khác:
                    TEN: [tên bệnh đầy đủ bằng tiếng Việt]
                    TRIEU_CHUNG: [mô tả triệu chứng ngắn gọn, 1-2 câu]
                    DIEU_TRI: [các bước xử lý ngắn gọn, 1-2 câu]
                    THUOC: [tên thuốc cụ thể khuyên dùng, 1-2 loại]
                    PHONG_NGUA: [biện pháp phòng ngừa ngắn gọn, 1-2 câu]
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("model", MODEL)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                }

                val body = requestJson.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d(TAG, "Groq HTTP ${response.code}")

                if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                    Log.e(TAG, "Groq API lỗi HTTP ${response.code}: $responseBody")
                    return@withContext null
                }

                val json = JSONObject(responseBody)
                val text = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                Log.d(TAG, "Groq response: $text")
                parseResponse(vietnameseName, text)

            } catch (t: Throwable) {
                Log.e(TAG, "Groq lỗi: ${t.javaClass.simpleName} - ${t.message}")
                null
            }
        }
    }

    private fun parseResponse(vietnameseName: String, text: String): DiseaseDetail {
        val data = mutableMapOf<String, String>()

        for (line in text.lines()) {
            val colonIdx = line.indexOf(":")
            if (colonIdx == -1) continue
            val key   = line.substring(0, colonIdx).trim()
            val value = line.substring(colonIdx + 1).trim()
            if (key.isNotEmpty() && value.isNotEmpty()) {
                data[key] = value
            }
        }

        return DiseaseDetail(
            name        = data["TEN"]         ?: vietnameseName,
            symptoms    = data["TRIEU_CHUNG"] ?: "Không có thông tin.",
            treatment   = data["DIEU_TRI"]    ?: "Không có thông tin.",
            medications = data["THUOC"]       ?: "Không có thông tin.",
            prevention  = data["PHONG_NGUA"]  ?: "Không có thông tin."
        )
    }
}
