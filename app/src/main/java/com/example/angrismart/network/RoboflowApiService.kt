package com.example.angrismart.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface RoboflowApiService {
    @Multipart
    @POST("rice-disease-rwuhc/3")
    suspend fun detectDisease(
        @Query("api_key") apiKey: String,
        @Part file: MultipartBody.Part
    ): RoboflowResponse

    companion object {
        private const val BASE_URL = "https://detect.roboflow.com/"

        fun create(): RoboflowApiService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RoboflowApiService::class.java)
        }
    }
}
