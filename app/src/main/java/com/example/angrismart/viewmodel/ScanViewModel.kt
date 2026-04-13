package com.example.angrismart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.data.remote.RetrofitClient
import com.example.angrismart.data.remote.model.PredictResponse
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import retrofit2.HttpException
import java.io.IOException


class ScanViewModel : ViewModel() {

    private val _scanState = MutableStateFlow<Resource<PredictResponse>?>(null)
    val scanState: StateFlow<Resource<PredictResponse>?> = _scanState.asStateFlow()

    fun analyzeImage(photoFile: File) {
        // Bắt đầu hiện vòng xoay Đang tải
        _scanState.value = Resource.Loading()
        
        viewModelScope.launch {
            try {
                // Biến đổi File Ảnh cục bộ thành Gói Ảnh Gửi Qua Internet
                val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", photoFile.name, requestFile)
                
                // Trực tiếp gọi AI Server của bạn (Sẽ bị Block cho đến khi Server trả lời xong)
                val response = RetrofitClient.apiService.detectDisease(imagePart)
                
                // Thành công
                _scanState.value = Resource.Success(response)
                
            } catch (e: HttpException) {
                // Lỗi 4xx, 5xx từ FastAPI
                _scanState.value = Resource.Error("Lỗi Hệ thống AI: ${e.code()}")
            } catch (e: IOException) {
                // Lỗi không có mạng hoặc Server bị sập/chưa mở
                _scanState.value = Resource.Error("Máy chủ phân tích bệnh đang ngoại tuyến!")
            } catch (e: Exception) {
                _scanState.value = Resource.Error(e.localizedMessage ?: "Quá trình quét bị gián đoạn.")
            }
        }
    }

    // Call hàm này sau khi Màn hình nhận được Result rồi để reset vòng xoay Loading
    fun resetState() {
        _scanState.value = null
    }
}
