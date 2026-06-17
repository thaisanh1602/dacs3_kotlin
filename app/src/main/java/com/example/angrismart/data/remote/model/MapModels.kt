package com.example.angrismart.data.remote.model

data class NominatimResponse(
    val lat: String,
    val lon: String,
    val display_name: String
)

data class OverpassResponse(
    val elements: List<OverpassElement>
)

data class OverpassElement(
    val type: String, // "way" or "relation"
    val id: Long,
    val tags: Map<String, String>?,
    val geometry: List<GeoPointDetail>?
)

data class GeoPointDetail(
    val lat: Double,
    val lon: Double
)
