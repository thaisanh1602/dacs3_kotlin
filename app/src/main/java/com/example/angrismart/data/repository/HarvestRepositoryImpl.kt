package com.example.angrismart.data.repository

import com.example.angrismart.domain.model.Harvest
import com.example.angrismart.domain.repository.HarvestRepository
import com.example.angrismart.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class HarvestRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : HarvestRepository {

    private val collection = firestore.collection("harvests")

    /**
     * Lưu thu hoạch lên Firestore.
     * profit và totalRevenue đã được tính ở ViewModel trước khi gọi hàm này.
     */
    override suspend fun addHarvest(harvest: Harvest): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val docRef = collection.document()
            val newHarvest = harvest.copy(id = docRef.id)
            docRef.set(newHarvest).await()
            emit(Resource.Success(docRef.id))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Lỗi khi lưu dữ liệu thu hoạch!"))
        }
    }

    /** Realtime listener — toàn bộ thu hoạch của người dùng, sắp xếp mới nhất trước */
    override fun getHarvestsByUser(userId: String): Flow<Resource<List<Harvest>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = collection
            .whereEqualTo("user_uid", userId)
            // Không dùng orderBy ở đây để tránh cần Composite Index trên Firestore
            // Thay vào đó sort in-memory bên dưới
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Lỗi tải dữ liệu thu hoạch"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(Harvest::class.java) }
                    ?.sortedByDescending { it.harvestDate?.seconds }
                    ?: emptyList()
                trySend(Resource.Success(list))
            }

        awaitClose { subscription.remove() }
    }

    /** Realtime listener — thu hoạch theo từng ruộng */
    override fun getHarvestsByField(fieldId: String): Flow<Resource<List<Harvest>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = collection
            .whereEqualTo("field_id", fieldId)
            // Không dùng orderBy ở đây để tránh cần Composite Index trên Firestore
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Lỗi tải dữ liệu thu hoạch"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(Harvest::class.java) }
                    ?.sortedByDescending { it.harvestDate?.seconds }
                    ?: emptyList()
                trySend(Resource.Success(list))
            }

        awaitClose { subscription.remove() }
    }
}
