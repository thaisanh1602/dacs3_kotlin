package com.example.angrismart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.data.remote.WeatherRetrofitClient
import com.example.angrismart.data.remote.model.CurrentWeather
import com.example.angrismart.data.remote.model.WeatherForecastResponse
import com.example.angrismart.utils.DiseaseRiskChecker
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    // Thời tiết hôm nay
    private val _currentWeather = MutableStateFlow<Resource<CurrentWeather>>(Resource.Loading())
    val currentWeather: StateFlow<Resource<CurrentWeather>> = _currentWeather.asStateFlow()

    // Dự báo 7 ngày tới
    private val _dailyForecast = MutableStateFlow<Resource<com.example.angrismart.data.remote.model.DailyForecast>>(Resource.Loading())
    val dailyForecast: StateFlow<Resource<com.example.angrismart.data.remote.model.DailyForecast>> = _dailyForecast.asStateFlow()

    // Cảnh báo bệnh (nếu có)
    private val _diseaseRisk = MutableStateFlow<DiseaseRiskChecker.RiskResult?>(null)
    val diseaseRisk: StateFlow<DiseaseRiskChecker.RiskResult?> = _diseaseRisk.asStateFlow()

    init {
        loadWeather()
    }

    fun loadWeather(lat: Double = 10.762622, lon: Double = 106.660172) {
        viewModelScope.launch {
            _currentWeather.value = Resource.Loading()
            try {
                // Lấy thời tiết dựa trên toạ độ
                val response = WeatherRetrofitClient.weatherService.getForecast(
                    lat = lat,
                    lon = lon
                )

                // Trạng thái thời tiết hôm nay và 7 ngày tới
                _currentWeather.value = Resource.Success(response.currentWeather)
                _dailyForecast.value = Resource.Success(response.daily)

                // Kiểm tra nguy cơ bệnh 7 ngày tới
                val riskResult = DiseaseRiskChecker.checkRisk(
                    dates = response.daily.time,
                    tempMax = response.daily.tempMax,
                    tempMin = response.daily.tempMin,
                    humidityMax = response.daily.humidityMax
                )
                _diseaseRisk.value = riskResult

            } catch (e: Exception) {
                _currentWeather.value = Resource.Error("Lỗi tải thời tiết: ${e.message}")
                _dailyForecast.value = Resource.Error("Lỗi dữ liệu báo trước.")
            }
        }
    }

    // Chuyển mã WMO thành emoji + mô tả tiếng Việt
    companion object {
        fun weatherDescription(code: Int): Pair<String, String> {
            return when (code) {
                0 -> "☀️" to "Trời nắng"
                1, 2, 3 -> "⛅" to "Ít mây"
                45, 48 -> "🌫️" to "Sương mù"
                51, 53, 55 -> "🌦️" to "Mưa phùn"
                61, 63, 65 -> "🌧️" to "Mưa"
                71, 73, 75 -> "❄️" to "Tuyết"
                80, 81, 82 -> "🌧️" to "Mưa rào"
                95, 96, 99 -> "⛈️" to "Giông bão"
                else -> "🌤️" to "Quang đãng"
            }
        }
    }
}
