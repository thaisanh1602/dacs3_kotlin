package com.example.angrismart.domain.model

import com.google.firebase.firestore.PropertyName

data class SeasonTemplate(
    val id: String = "",
    @get:PropertyName("season_name")
    @set:PropertyName("season_name")
    var seasonName: String = "",
    @get:PropertyName("start_month")
    @set:PropertyName("start_month")
    var startMonth: Int = 1,
    @get:PropertyName("start_day")
    @set:PropertyName("start_day")
    var startDay: Int = 1,
    @get:PropertyName("end_month")
    @set:PropertyName("end_month")
    var endMonth: Int = 12,
    @get:PropertyName("end_day")
    @set:PropertyName("end_day")
    var endDay: Int = 31
)
