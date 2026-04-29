package com.example.angrismart.data.remote

import com.example.angrismart.data.remote.model.PredictResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    // API Nhận diện Bệnh bằng Hình ảnh
    @Multipart
    @POST("api/v1/disease/detect")
    suspend fun detectDisease(
        @Part file: MultipartBody.Part
    ): PredictResponse

    // API Chat chuyên gia tư vấn Nông nghiệp
    @retrofit2.http.Headers("Content-Type: application/json; charset=UTF-8")
    @POST("/api/v1/chat/chat")
    suspend fun sendChatMessage(
        @retrofit2.http.Body requestBody: com.example.angrismart.data.remote.model.ChatRequest
    ): retrofit2.Response<com.example.angrismart.data.remote.model.ChatResponse>

    // API Kiểm tra trạng thái hệ thống (Health Check)
    @retrofit2.http.GET("/")
    suspend fun checkHealth(): retrofit2.Response<com.example.angrismart.data.remote.model.HealthResponse>
}
