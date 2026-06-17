package com.example.angrismart.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MapRetrofitClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val mapService: MapApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MapApiService::class.java)
    }
}
