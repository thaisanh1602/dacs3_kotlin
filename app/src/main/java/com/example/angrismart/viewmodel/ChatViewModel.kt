package com.example.angrismart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.data.remote.RetrofitClient
import com.example.angrismart.data.remote.model.ChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Chào bà con! Tôi là hệ thống chuyên gia nông nghiệp AI.\nBà con có câu hỏi nào về canh tác hay loại thuốc phun chữa sâu bệnh không?", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        // Cập nhật giao diện: Hiện tin nhắn người dùng nhập lên
        val currentList = _messages.value.toMutableList()
        currentList.add(ChatMessage(text, isUser = true))
        _messages.value = currentList

        // Hiện nút quay tròn chờ AI
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Gọi tới máy chủ FastAPI
                val response = RetrofitClient.apiService.sendChatMessage(ChatRequest(message = text))
                
                if (response.isSuccessful) {
                    val reply = response.body()?.reply ?: "Xin lỗi, tôi không thể trả lời lúc này."
                    _messages.value = _messages.value + ChatMessage(reply, isUser = false)
                } else {
                    _messages.value = _messages.value + ChatMessage("Lỗi máy chủ AI: ${response.code()}", isUser = false)
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Lỗi kết nối mạng: ${e.message}", isUser = false)
            } finally {
                // Tắt nháy quay tròn
                _isLoading.value = false
            }
        }
    }
}
