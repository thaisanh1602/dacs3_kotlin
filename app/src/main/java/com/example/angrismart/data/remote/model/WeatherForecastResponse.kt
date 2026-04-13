package com.example.angrismart.data.remote.model

import com.google.gson.annotations.SerializedName

// Phản hồi JSON gốc từ OpenMeteo
data class WeatherForecastResponse(
    @SerializedName("current_weather")
    val currentWeather: CurrentWeather,

    val daily: DailyForecast
)

data class CurrentWeather(
    val temperature: Double,      // Nhiệt độ hiện tại (°C)
    val windspeed: Double,        // Tốc độ gió (km/h)
    val weathercode: Int          // Mã thời tiết (WMO)
)

data class DailyForecast(
    val time: List<String>,                       // ["2026-03-29", "2026-03-30", ...]
    
    @SerializedName("temperature_2m_max")
    val tempMax: List<Double>,                    // Nhiệt độ cao nhất mỗi ngày

    @SerializedName("temperature_2m_min")
    val tempMin: List<Double>,                    // Nhiệt độ thấp nhất mỗi ngày

    @SerializedName("relative_humidity_2m_max")
    val humidityMax: List<Int?>?                  // Độ ẩm cao nhất mỗi ngày
)
