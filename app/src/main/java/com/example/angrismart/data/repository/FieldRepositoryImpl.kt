package com.example.angrismart.data.repository

import android.util.Log
import com.example.angrismart.domain.model.Farm
import com.example.angrismart.domain.repository.FieldRepository
import com.example.angrismart.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FieldRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FieldRepository {

    // Lấy danh sách đồng ruộng theo Thời gian thực (Cập nhật từ thiết bị khác sẽ nhảy sang máy này tức thì)
    override fun getFarms(userId: String): Flow<Resource<List<Farm>>> = callbackFlow {
        trySend(Resource.Loading()) // Gửi cờ báo đang tải

        val subscription = firestore.collection("Fields")
            .whereEqualTo("user_uid", userId)
            // Lắng nghe liên tục
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Lỗi csdl khi tải dữ liệu"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val farms = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Farm::class.java)
                        } catch (e: Exception) {
                            Log.e("FieldRepo", "Error parsing farm: ${e.message}")
                            null
                        }
                    }
                    trySend(Resource.Success(farms))
                } else {
                    trySend(Resource.Success(emptyList()))
                }
            }
        
        // Hủy lắng nghe tài nguyên mạng để tiết kiệm Pin khi người dùng thoát App
        awaitClose { subscription.remove() }
    }

    override suspend fun addFarm(farm: Farm): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            // Firestore tự sinh ID
            val docRef = firestore.collection("Fields").document()
            
            // Xoá ID null và nạp ID do Firebase cấp
            val newFarm = farm.copy(id = docRef.id) 
            
            // Nếu có latitude/longitude thì chuyển thành map location trước khi lưu (nếu cần, nhưng Farm đã có location map)
            // Đẩy dữ liệu lên
            docRef.set(newFarm).await()

            emit(Resource.Success(docRef.id)) // Báo tín hiệu thêm thành công
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Cố lỗi khi lưu đồng ruộng!"))
        }
    }

    override suspend fun updateFarm(farm: Farm): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("Fields").document(farm.id).set(farm).await()
            emit(Resource.Success("Cập nhật thành công"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Lỗi khi cập nhật dữ liệu!"))
        }
    }
}
