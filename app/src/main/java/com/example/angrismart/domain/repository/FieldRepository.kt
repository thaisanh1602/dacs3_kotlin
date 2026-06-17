package com.example.angrismart.domain.repository

import com.example.angrismart.domain.model.Farm
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.Flow

interface FieldRepository {
    // Trả về dòng chảy dữ liệu tự động cập nhật (Realtime) của danh sách ruộng
    fun getFarms(userId: String): Flow<Resource<List<Farm>>>
    
    // Thêm một thửa ruộng mới vào cơ sở dữ liệu
    suspend fun addFarm(farm: Farm): Flow<Resource<String>>

    // Cập nhật thông tin ruộng (ví dụ: ngày gieo sạ)
    suspend fun updateFarm(farm: Farm): Flow<Resource<String>>

    // Xóa một thửa ruộng
    suspend fun deleteFarm(farmId: String): Flow<Resource<String>>
}

