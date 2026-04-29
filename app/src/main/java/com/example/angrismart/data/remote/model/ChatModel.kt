package com.example.angrismart.data.remote.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val status: String,
    val answer: String
)
