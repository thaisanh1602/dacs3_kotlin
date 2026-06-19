package com.example.angrismart.data.remote.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val message: String,
    @SerializedName("session_id")
    val sessionId: String? = null
)

data class ChatResponse(
    val status: String,
    val answer: String
)
