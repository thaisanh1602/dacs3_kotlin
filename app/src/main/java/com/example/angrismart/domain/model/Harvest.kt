package com.example.angrismart.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Mô hình dữ liệu Thu Hoạch — ánh xạ sang Firestore collection "harvests".
 *
 * Lợi nhuận được tính và lưu tại đây:
 *   profit = (totalWeight × salePrice) - totalExpense
 */
data class Harvest(
    @DocumentId val id: String = "",

    @get:PropertyName("field_id")
    @set:PropertyName("field_id")
    var fieldId: String = "",

    @get:PropertyName("user_uid")
    @set:PropertyName("user_uid")
    var userUid: String = "",

    @get:PropertyName("variant_name")
    @set:PropertyName("variant_name")
    var variantName: String = "",

    /** Tổng cân nặng thu hoạch (kg) */
    @get:PropertyName("total_weight")
    @set:PropertyName("total_weight")
    var totalWeight: Double = 0.0,

    /** Giá bán mỗi kg (VNĐ) */
    @get:PropertyName("sale_price")
    @set:PropertyName("sale_price")
    var salePrice: Double = 0.0,

    /** Tổng chi phí trong vụ (VNĐ) */
    @get:PropertyName("total_expense")
    @set:PropertyName("total_expense")
    var totalExpense: Double = 0.0,

    /** Tổng doanh thu = totalWeight × salePrice (VNĐ) — tính và lưu tự động */
    @get:PropertyName("total_revenue")
    @set:PropertyName("total_revenue")
    var totalRevenue: Double = 0.0,

    /** Lợi nhuận = totalRevenue - totalExpense (VNĐ) — tính và lưu tự động */
    @get:PropertyName("profit")
    @set:PropertyName("profit")
    var profit: Double = 0.0,

    @get:PropertyName("harvest_date")
    @set:PropertyName("harvest_date")
    var harvestDate: Timestamp = Timestamp.now(),

    /** Tên vụ mùa, ví dụ: "Đông Xuân 2026" */
    @get:PropertyName("crop_season")
    @set:PropertyName("crop_season")
    var cropSeason: String = "",
)
