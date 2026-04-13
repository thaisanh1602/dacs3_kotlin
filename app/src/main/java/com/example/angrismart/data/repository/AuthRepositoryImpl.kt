package com.example.angrismart.data.repository

import com.example.angrismart.domain.repository.AuthRepository
import com.example.angrismart.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override suspend fun loginWithEmail(email: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading()) // Phát tin hiệu: Màn hình hiển thị vòng xoay đang Load
        try {
            // Gửi API lên Firebase để đăng nhập
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Mật khẩu sai hoặc tài khoản không tồn tại")
            
            if (!user.isEmailVerified) {
                // Chưa xác thực -> Đá văng ra bắt xác thực mới cho xài app
                auth.signOut()
                throw Exception("⚠️ Tài khoản chưa kích hoạt!\nVui lòng vào hộp thư Gmail của bạn để bấm link xác thực.")
            }
            
            emit(Resource.Success(user.uid)) // Thực sự thành công, cho vô app
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Cố lỗi khi đăng nhập. Vui lòng thử lại!"))
        }
    }

    override suspend fun registerWithEmail(email: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Không thể tạo thông tin người dùng")
            
            // Yêu cầu Firebase phóng 1 cái Email xác thực tới Gmail của bà con
            user.sendEmailVerification().await()
            
            // Xoá kết nối ảo để bắt người ta phải login thủ công sau khi Verify Email
            auth.signOut()
            
            emit(Resource.Success("REQUIRE_VERIFICATION"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Lỗi đăng ký"))
        }
    }

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null
    override fun getCurrentUserId(): String? = auth.currentUser?.uid
    override fun logout() = auth.signOut()
}
