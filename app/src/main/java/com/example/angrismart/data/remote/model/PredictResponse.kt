package com.example.angrismart.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Phản hồi thực tế từ AI Server ResNet18
 * JSON: { "prediction": "brown_spot", "confidence": 0.985, "filename": "leaf_image.jpg" }
 */
data class PredictResponse(
    @SerializedName("prediction")
    val prediction: String,
    
    @SerializedName("confidence")
    val confidence: Double,
    
    @SerializedName("filename")
    val filename: String
)
