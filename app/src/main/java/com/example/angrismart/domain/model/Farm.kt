package com.example.angrismart.domain.model

import com.google.firebase.firestore.PropertyName

data class Farm(
    val id: String = "",
    
    @get:PropertyName("user_uid")
    @set:PropertyName("user_uid")
    var userId: String = "",
    
    @get:PropertyName("field_name")
    @set:PropertyName("field_name")
    var farmName: String = "",
    
    @get:PropertyName("current_rice_variant_id")
    @set:PropertyName("current_rice_variant_id")
    var varietyId: String = "",
    
    @get:PropertyName("age_days")
    @set:PropertyName("age_days")
    var ageDays: Int = 0,
    
    @get:PropertyName("total_growth_days")
    @set:PropertyName("total_growth_days")
    var totalGrowthDays: Int = 100,

    @get:PropertyName("sowing_date")
    @set:PropertyName("sowing_date")
    var sowingDate: com.google.firebase.Timestamp? = null,

    @get:PropertyName("area")
    @set:PropertyName("area")
    var areaM2: Double = 0.0,
    
    var location: Map<String, Double>? = null,
    
    var status: String = "active",

    @get:PropertyName("is_harvested")
    @set:PropertyName("is_harvested")
    var isHarvested: Int = 0
) {
    val latitude: Double? get() = location?.get("latitude")
    val longitude: Double? get() = location?.get("longitude")
}
