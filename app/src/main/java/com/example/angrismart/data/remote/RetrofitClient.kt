package com.example.angrismart.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Thay bằng IP mạng LAN của máy tính (ví dụ 192.168.x.x) nếu chạy điện thoại thật
    private const val BASE_URL = "http://192.168.5.156:8000/api/v1/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Tự động phân tách JSON -> Kotlin Object
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
