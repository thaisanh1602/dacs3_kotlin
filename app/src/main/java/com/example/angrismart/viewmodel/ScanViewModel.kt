package com.example.angrismart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.network.GroqService
import com.example.angrismart.network.HttpStatusException
import com.example.angrismart.network.RoboflowApiService
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ScanResultData(
    val diseaseName: String,
    val confidence: String,
    val description: String,
    val treatment: String,
    val riskLevel: String
)

class ScanViewModel : ViewModel() {

    private val _scanState = MutableStateFlow<Resource<ScanResultData>?>(null)
    val scanState: StateFlow<Resource<ScanResultData>?> = _scanState.asStateFlow()

    fun analyzeImage(photoFile: File) {
        _scanState.value = Resource.Loading()

        viewModelScope.launch {
            try {
                // 1. Phân tích ảnh bằng Roboflow serverless (rice-leaf-wfax3/2)
                //    Gửi base64 raw body với Content-Type: application/x-www-form-urlencoded
                val predictions = retryOnError(maxRetries = 2) {
                    RoboflowApiService.detectDisease(photoFile)
                }

                if (predictions.isEmpty()) {
                    _scanState.value = Resource.Error("Không phát hiện thấy bệnh trên lá lúa.")
                    return@launch
                }

                // Lấy kết quả có độ tin cậy cao nhất
                val best           = predictions.maxByOrNull { it.confidence }!!
                val confidencePercent = "${(best.confidence * 100).toInt()}%"

                // Nếu healthy → thông báo inline, không điều hướng sang màn kết quả
                if (best.className.lowercase() == "healthy") {
                    _scanState.value = Resource.Error("✅ Lá lúa khỏe mạnh, không phát hiện bệnh.")
                    return@launch
                }

                // 2. Lấy thông tin chi tiết bệnh bằng Groq AI
                val diseaseDetail = GroqService.getDiseaseInfo(best.className)

                if (diseaseDetail != null) {
                    _scanState.value = Resource.Success(
                        ScanResultData(
                            diseaseName = diseaseDetail.name,
                            confidence  = confidencePercent,
                            description = diseaseDetail.symptoms,
                            treatment   = "${diseaseDetail.treatment}\nThuốc: ${diseaseDetail.medications}",
                            riskLevel   = "Cao"
                        )
                    )
                } else {
                    _scanState.value = Resource.Error("Không thể lấy thông tin từ AI. Vui lòng thử lại sau.")
                }

            } catch (e: HttpStatusException) {
                val msg = when (e.code) {
                    503 -> "Máy chủ AI đang quá tải (503). Vui lòng thử lại sau vài giây."
                    401 -> "API key không hợp lệ. Vui lòng kiểm tra cấu hình."
                    429 -> "Vượt giới hạn số lần gọi API. Vui lòng thử lại sau."
                    404 -> "Không tìm thấy model. Vui lòng kiểm tra model ID."
                    else -> "Lỗi máy chủ (HTTP ${e.code}). Vui lòng thử lại."
                }
                _scanState.value = Resource.Error(msg)
            } catch (e: java.net.SocketTimeoutException) {
                _scanState.value = Resource.Error("Kết nối bị timeout. Kiểm tra mạng và thử lại.")
            } catch (e: java.io.IOException) {
                _scanState.value = Resource.Error("Lỗi kết nối mạng. Vui lòng kiểm tra Internet và thử lại.")
            } catch (e: Exception) {
                _scanState.value = Resource.Error(e.localizedMessage ?: "Quá trình quét bị gián đoạn.")
            }
        }
    }

    /**
     * Tự động retry khi gặp HTTP 503 hoặc SocketTimeoutException.
     * Exponential backoff: 2s → 4s giữa các lần thử.
     */
    private suspend fun <T> retryOnError(maxRetries: Int, block: suspend () -> T): T {
        var lastException: Exception? = null
        repeat(maxRetries + 1) { attempt ->
            try {
                return block()
            } catch (e: HttpStatusException) {
                if (e.code == 503 && attempt < maxRetries) {
                    lastException = e
                    kotlinx.coroutines.delay(2000L * (attempt + 1))
                } else throw e
            } catch (e: java.net.SocketTimeoutException) {
                if (attempt < maxRetries) {
                    lastException = e
                    kotlinx.coroutines.delay(2000L * (attempt + 1))
                } else throw e
            }
        }
        throw lastException!!
    }

    fun resetState() {
        _scanState.value = null
    }
}
