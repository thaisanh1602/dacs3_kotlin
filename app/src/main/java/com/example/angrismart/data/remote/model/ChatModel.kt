package com.example.angrismart.data.remote.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val message: String,
    @SerializedName("session_id")
    val sessionId: String? = null
)

data class ChatResponse(
    val reply: String,
    @SerializedName("source_documents")
    val sourceDocuments: List<String>? = null
)
