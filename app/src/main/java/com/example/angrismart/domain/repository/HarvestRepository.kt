package com.example.angrismart.domain.repository

import com.example.angrismart.domain.model.Harvest
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.Flow

interface HarvestRepository {
    /** Lưu một bản ghi thu hoạch lên Firestore. Trả về ID document mới. */
    suspend fun addHarvest(harvest: Harvest): Flow<Resource<String>>

    /** Lấy toàn bộ lịch sử thu hoạch của một người dùng (realtime). */
    fun getHarvestsByUser(userId: String): Flow<Resource<List<Harvest>>>

    /** Lấy danh sách thu hoạch theo ruộng cụ thể, chỉ trả về của người dùng hiện tại (realtime). */
    fun getHarvestsByField(fieldId: String, userId: String): Flow<Resource<List<Harvest>>>
}
