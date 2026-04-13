package com.example.angrismart.data.remote

import com.example.angrismart.data.remote.model.WeatherForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    // API miễn phí OpenMeteo (KHÔNG CẦN API KEY)
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,relative_humidity_2m_max",
        @Query("timezone") timezone: String = "Asia/Ho_Chi_Minh",
        @Query("forecast_days") forecastDays: Int = 7
    ): WeatherForecastResponse
}
