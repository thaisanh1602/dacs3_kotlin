package com.example.angrismart.network

import com.google.gson.annotations.SerializedName

data class RoboflowResponse(
    @SerializedName("predictions")
    val predictions: List<Prediction>,
    @SerializedName("image")
    val image: ImageInfo
)

data class Prediction(
    @SerializedName("x")
    val x: Double,
    @SerializedName("y")
    val y: Double,
    @SerializedName("width")
    val width: Double,
    @SerializedName("height")
    val height: Double,
    @SerializedName("confidence")
    val confidence: Double,
    @SerializedName("class")
    val className: String,
    @SerializedName("image_path")
    val imagePath: String? = null,
    @SerializedName("prediction_type")
    val predictionType: String? = null
)

data class ImageInfo(
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int
)
