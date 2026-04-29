package com.example.angrismart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.network.DiseaseDetail
import com.example.angrismart.network.GroqService
import com.example.angrismart.network.RoboflowApiService
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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

    private val apiService = RoboflowApiService.create()
    private val apiKey = "pXB6PH9xrIvBq48CQB9X"

    fun analyzeImage(photoFile: File) {
        _scanState.value = Resource.Loading()
        
        viewModelScope.launch {
            try {
                // 1. Phân tích ảnh bằng Roboflow
                val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", photoFile.name, requestFile)
                
                val response = apiService.detectDisease(apiKey, imagePart)
                
                if (response.predictions.isEmpty()) {
                    _scanState.value = Resource.Error("Không phát hiện thấy bệnh hoặc cây khoẻ mạnh.")
                    return@launch
                }
                
                // Lấy kết quả có độ tin cậy cao nhất
                val bestPrediction = response.predictions.maxByOrNull { it.confidence }!!
                val confidencePercent = "${(bestPrediction.confidence * 100).toInt()}%"
                
                if (bestPrediction.className.lowercase() == "healthy") {
                    _scanState.value = Resource.Success(
                        ScanResultData(
                            diseaseName = "Cây lúa khỏe mạnh",
                            confidence = confidencePercent,
                            description = "Cây lúa không có dấu hiệu sâu bệnh.",
                            treatment = "Tiếp tục chăm sóc bình thường.",
                            riskLevel = "Thấp"
                        )
                    )
                    return@launch
                }

                // 2. Lấy thông tin chi tiết bằng Groq AI
                val diseaseDetail = GroqService.getDiseaseInfo(bestPrediction.className)
                
                if (diseaseDetail != null) {
                    _scanState.value = Resource.Success(
                        ScanResultData(
                            diseaseName = diseaseDetail.name,
                            confidence = confidencePercent,
                            description = diseaseDetail.symptoms,
                            treatment = "${diseaseDetail.treatment}\nThuốc: ${diseaseDetail.medications}",
                            riskLevel = "Cao" // Groq Service không trả về RiskLevel rõ, ta mặc định là Cần lưu ý
                        )
                    )
                } else {
                    _scanState.value = Resource.Error("Không thể lấy thông tin từ Groq AI. Vui lòng thử lại sau.")
                }

            } catch (e: Exception) {
                _scanState.value = Resource.Error(e.localizedMessage ?: "Quá trình quét bị gián đoạn.")
            }
        }
    }

    fun resetState() {
        _scanState.value = null
    }
}
