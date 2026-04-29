package com.example.angrismart.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Farm(
    @DocumentId val id: String = "",
    var userId: String = "",
    
    @get:PropertyName("farm_name")
    @set:PropertyName("farm_name")
    var farmName: String = "",
    
    @get:PropertyName("variety_name")
    @set:PropertyName("variety_name")
    var varietyName: String = "",
    
    @get:PropertyName("age_days")
    @set:PropertyName("age_days")
    var ageDays: Int = 0,
    
    @get:PropertyName("total_growth_days")
    @set:PropertyName("total_growth_days")
    var totalGrowthDays: Int = 100, // Thường 90-110 ngày tuỳ giống lúa

    @get:PropertyName("area_m2")
    @set:PropertyName("area_m2")
    var areaM2: Double = 0.0,
    
    var latitude: Double? = null,
    var longitude: Double? = null,
    
    val status: String = "active" // active, harvested
)
