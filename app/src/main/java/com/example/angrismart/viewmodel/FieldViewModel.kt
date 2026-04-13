package com.example.angrismart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.data.repository.FieldRepositoryImpl
import com.example.angrismart.domain.model.Farm
import com.example.angrismart.domain.repository.FieldRepository
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FieldViewModel(
    private val repository: FieldRepository = FieldRepositoryImpl(),
    private val currentUserId: String = "debug_user_123" // Giả lập Auth UserId, sau này truyền từ AuthViewModel
) : ViewModel() {

    // Danh sách Cánh Đồng được quản lý Live dưới dạng StateFlow
    private val _farmsState = MutableStateFlow<Resource<List<Farm>>>(Resource.Loading())
    val farmsState: StateFlow<Resource<List<Farm>>> = _farmsState.asStateFlow()

    // Trạng thái Khi Thêm Farm
    private val _addFarmState = MutableStateFlow<Resource<String>?>(null)
    val addFarmState: StateFlow<Resource<String>?> = _addFarmState.asStateFlow()

    init {
        // Tự động load dữ liệu khi màn hình mở lên
        loadFarms()
    }

    fun loadFarms() {
        viewModelScope.launch {
            repository.getFarms(currentUserId).collect { result ->
                _farmsState.value = result
            }
        }
    }

    fun addFarm(farmName: String, varietyName: String, areaM2: String) {
        if (farmName.isBlank() || varietyName.isBlank() || areaM2.isBlank()) {
            _addFarmState.value = Resource.Error("Vui lòng điền đủ thông tin thửa ruộng!")
            return
        }

        val area = areaM2.toDoubleOrNull()
        if (area == null || area <= 0) {
            _addFarmState.value = Resource.Error("Diện tích phải là một số lớn hơn 0!")
            return
        }

        viewModelScope.launch {
            val newFarm = Farm(
                userId = currentUserId,
                farmName = farmName,
                varietyName = varietyName,
                areaM2 = area,
                ageDays = 0, // Vừa gieo sạ thì bằng 0
                totalGrowthDays = 95
            )

            repository.addFarm(newFarm).collect { result ->
                _addFarmState.value = result
            }
        }
    }

    // Xoá trạng thái lưu lại màn hình Thêm để tránh popup liên tục
    fun resetAddFarmState() {
        _addFarmState.value = null
    }
}
