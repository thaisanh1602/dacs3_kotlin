package com.example.angrismart.utils

/**
 * Lớp ánh xạ từ mã bệnh (English - Server) sang thông tin chi tiết (Tiếng Việt - App)
 */
object DiseaseMapper {

    data class DiseaseInfo(
        val nameVi: String,
        val riskLevel: String, // High, Medium, Low
        val description: String,
        val treatment: String
    )

    fun getInfo(prediction: String): DiseaseInfo {
        return when (prediction.lowercase()) {
            "brown_spot" -> DiseaseInfo(
                nameVi = "Bệnh Đốm Nâu (Brown Spot)",
                riskLevel = "Medium",
                description = "Do nấm Cochliobolus miyabeanus. Xuất hiện các vết đốm màu nâu tròn hoặc bầu dục trên lá, thường ở các vùng đất thiếu dinh dưỡng hoặc phèn.",
                treatment = "1. Bón cân đối phân NPK, đặc biệt là Kali.\n2. Phun thuốc trừ nấm: Tilt Super 300EC, Nevo 330EC hoặc Amistar Top."
            )
            "leaf_blast" -> DiseaseInfo(
                nameVi = "Bệnh Đạo Ôn (Leaf Blast)",
                riskLevel = "High",
                description = "Do nấm Pyricularia oryzae. Vết bệnh có hình thoi, tâm màu xám trắng. Đây là bệnh nguy hiểm nhất có thể gây cháy lá hàng loạt.",
                treatment = "1. Ngưng bón phân đạm ngay lập tức.\n2. Giữ mực nước ổn định trên ruộng.\n3. Phun thuốc: Beam 75WP, Blast 25SC hoặc Trizole 75WP."
            )
            "bacterial_leaf_blight" -> DiseaseInfo(
                nameVi = "Bệnh Bạc Lá (Bacterial Leaf Blight)",
                riskLevel = "High",
                description = "Do vi khuẩn Xanthomonas oryzae. Vết bệnh từ rìa lá lan dần vào trong, có hình lượn sóng, màu vàng trắng hoặc xám trắng.",
                treatment = "1. Vệ sinh đồng ruộng sạch cỏ dại.\n2. Phun thuốc trừ khuẩn: Sansai 200WP, Totan 200WP hoặc Kasuran 47WP."
            )
            "leaf_scald" -> DiseaseInfo(
                nameVi = "Bệnh Thối Bẹ (Leaf Scald)",
                riskLevel = "Medium",
                description = "Do nấm Microdochium oryzae. Gây các vết loang lổ ở chóp lá hoặc mép lá, trông như bị nhúng nước sôi rồi khô lại.",
                treatment = "1. Tránh bón thừa phân đạm vào giai đoạn đòng trổ.\n2. Phun thuốc: Anvil 5SC hoặc Carbenzim 500FL."
            )
            "narrow_brown_spot" -> DiseaseInfo(
                nameVi = "Bệnh Đốm Nâu Hẹp (Narrow Brown Spot)",
                riskLevel = "Low",
                description = "Do nấm Cercospora janseana. Các vệt nâu ngắn, hẹp chạy song song với gân lá. Thường xuất hiện vào giai đoạn lúa sắp thu hoạch.",
                treatment = "1. Cày vùi rơm rạ sau thu hoạch.\n2. Bón lót vôi để cải thiện môi trường đất.\n3. Phun thuốc: Carbendazim 50WP."
            )
            "healthy" -> DiseaseInfo(
                nameVi = "Cây Lúa Khỏe Mạnh",
                riskLevel = "Low",
                description = "Chúc mừng! Mẫu lá cho thấy lúa đang phát triển tốt, không có dấu hiệu bệnh lý nguy hiểm.",
                treatment = "Tiếp tục theo dõi thời tiết và bón phân theo đúng quy trình kỹ thuật để đạt năng suất cao."
            )
            else -> DiseaseInfo(
                nameVi = "Dấu hiệu lạ: $prediction",
                riskLevel = "Medium",
                description = "Hệ thống phát hiện dấu hiệu không điển hình hoặc chưa được định danh rõ ràng.",
                treatment = "Vui lòng chụp lại ảnh rõ nét hơn hoặc tham khảo ý kiến chuyên gia bảo vệ thực vật địa phương."
            )
        }
    }
}
