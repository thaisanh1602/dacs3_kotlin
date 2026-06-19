package com.example.angrismart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.data.repository.AuthRepositoryImpl
import com.example.angrismart.domain.repository.AuthRepository
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout


class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    // Đây là cái loa báo hiệu: UI hãy thay đổi đi nếu cái này bằng Sucess, Loading hoặc Error
    private val _authState = MutableStateFlow<Resource<String>?>(null)
    val authState: StateFlow<Resource<String>?> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = Resource.Error("Bà con vui lòng nhập Email và Mật khẩu đầy đủ!")
            return
        }

        viewModelScope.launch {
            try {
                // Đề phòng Mạng kẹt, tự ép lỗi sau 10 giây để khỏi xoay hoài
                withTimeout(15000L) {
                    repository.loginWithEmail(email.trim(), pass.trim()).collect { result ->
                        _authState.value = result
                    }
                }
            } catch (e: Exception) {
                val errorMsg = if (e is kotlinx.coroutines.TimeoutCancellationException) {
                    "Lỗi: Rớt mạng (Timeout 15s). Máy ảo/Điện thoại của bạn đang bị mất kết nối Internet!"
                } else {
                    "Lỗi Đăng Nhập: ${e.message}"
                }
                _authState.value = Resource.Error(errorMsg)
            }
        }
    }

    // Chức năng Đăng ký tài khoản nhanh (Dành cho việc Test)
    fun register(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = Resource.Error("Vui lòng nhập Email và Mật khẩu để Đăng ký!")
            return
        }

        viewModelScope.launch {
            try {
                withTimeout(15000L) {
                    repository.registerWithEmail(email.trim(), pass.trim()).collect { result ->
                        _authState.value = result
                    }
                }
            } catch (e: Exception) {
                val errorMsg = if (e is kotlinx.coroutines.TimeoutCancellationException) {
                    "Lỗi: Rớt mạng (Timeout 15s). Máy ảo/Điện thoại của bạn đang bị mất kết nối Internet!"
                } else {
                    "Lỗi Đăng Ký: ${e.message}"
                }
                _authState.value = Resource.Error(errorMsg)
            }
        }
    }



    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = Resource.Error("Vui lòng nhập Email để đặt lại mật khẩu!")
            return
        }
        viewModelScope.launch {
            try {
                withTimeout(15000L) {
                    repository.sendPasswordResetEmail(email.trim()).collect { result ->
                        _authState.value = result
                    }
                }
            } catch (e: Exception) {
                _authState.value = Resource.Error("Lỗi: ${e.message}")
            }
        }
    }

    // Xoá màn hình báo lỗi cũ khi chuyển luồng
    fun resetState() {
        _authState.value = null
    }

    /** Đăng xuất tài khoản hiện tại — xóa phiên Firebase Auth và reset trạng thái UI */
    fun signOut() {
        repository.logout()
        _authState.value = null
    }
}
