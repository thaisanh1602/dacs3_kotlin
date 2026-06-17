package com.example.angrismart.data.remote

import com.example.angrismart.data.remote.model.NominatimResponse
import com.example.angrismart.data.remote.model.OverpassResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field

interface MapApiService {
    // Nominatim Search
    @GET("https://nominatim.openstreetmap.org/search")
    suspend fun searchLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1
    ): List<NominatimResponse>

    // Overpass API
    @FormUrlEncoded
    @POST("https://overpass-api.de/api/interpreter")
    suspend fun getFarmlands(
        @Field("data") query: String
    ): OverpassResponse
}
