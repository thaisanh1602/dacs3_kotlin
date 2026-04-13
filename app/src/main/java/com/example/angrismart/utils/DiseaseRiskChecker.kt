package com.example.angrismart.utils

// Quy tắc phát hiện nguy cơ dịch bệnh lúa dựa trên dữ liệu thời tiết 7 ngày
object DiseaseRiskChecker {

    data class RiskResult(
        val hasRisk: Boolean,
        val diseaseName: String,
        val riskDay: String,        // Ngày có nguy cơ
        val reason: String          // Mô tả điều kiện
    )

    /**
     * Quét toàn bộ 7 ngày dự báo, nếu BẤT KÌ ngày nào thoả điều kiện sinh bệnh -> Cảnh báo
     * 
     * Quy tắc dựa trên kiến thức Nông Học:
     * 1. Đạo Ôn (Blast): Nhiệt độ ban đêm < 22°C + Độ ẩm > 85%
     * 2. Bạc Lá (Bacterial Leaf Blight): Nhiệt độ max > 33°C + Độ ẩm > 80%  
     * 3. Khô Vằn (Sheath Blight): Nhiệt độ trung bình 28-34°C + Độ ẩm > 90%
     */
    fun checkRisk(
        dates: List<String>,
        tempMax: List<Double>,
        tempMin: List<Double>,
        humidityMax: List<Int?>?
    ): RiskResult {

        for (i in dates.indices) {
            val maxT = tempMax.getOrNull(i) ?: continue
            val minT = tempMin.getOrNull(i) ?: continue
            val humidity = humidityMax?.getOrNull(i) ?: continue

            // 1. Đạo ôn: Đêm lạnh + ẩm cao
            if (minT < 22.0 && humidity > 85) {
                return RiskResult(
                    hasRisk = true,
                    diseaseName = "Bệnh Đạo Ôn (Blast)",
                    riskDay = dates[i],
                    reason = "Nhiệt đêm ${minT}°C + Ẩm ${humidity}% → Bào tử nấm Pyricularia phát triển mạnh!"
                )
            }

            // 2. Bạc lá: Nóng gay gắt + ẩm
            if (maxT > 33.0 && humidity > 80) {
                return RiskResult(
                    hasRisk = true,
                    diseaseName = "Bệnh Bạc Lá (Bacterial Leaf Blight)",
                    riskDay = dates[i],
                    reason = "Nắng nóng ${maxT}°C + Ẩm ${humidity}% → Vi khuẩn Xanthomonas phát tán qua nước!"
                )
            }

            // 3. Khô vằn: Nóng ẩm kéo dài 
            val avgT = (maxT + minT) / 2
            if (avgT in 28.0..34.0 && humidity > 90) {
                return RiskResult(
                    hasRisk = true,
                    diseaseName = "Bệnh Khô Vằn (Sheath Blight)",
                    riskDay = dates[i],
                    reason = "Trung bình ${avgT}°C + Ẩm ${humidity}% → Nấm Rhizoctonia bùng phát!"
                )
            }
        }

        return RiskResult(hasRisk = false, diseaseName = "", riskDay = "", reason = "")
    }
}
