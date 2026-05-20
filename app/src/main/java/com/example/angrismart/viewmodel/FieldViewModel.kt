package com.example.angrismart.viewmodel

import com.google.firebase.auth.FirebaseAuth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.data.repository.FieldRepositoryImpl
import com.example.angrismart.domain.model.Farm
import com.example.angrismart.domain.model.RiceVariant
import com.example.angrismart.domain.repository.FieldRepository
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FieldViewModel(
    private val repository: FieldRepository = FieldRepositoryImpl(),
    private val masterRepository: com.example.angrismart.domain.repository.MasterDataRepository = com.example.angrismart.data.repository.MasterDataRepositoryImpl()
) : ViewModel() {

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Danh sách Cánh Đồng
    private val _farmsState = MutableStateFlow<Resource<List<Farm>>>(Resource.Loading())
    val farmsState: StateFlow<Resource<List<Farm>>> = _farmsState.asStateFlow()

    // Danh sách Giống lúa (để map ID -> Name)
    private val _riceVariantsState = MutableStateFlow<Resource<List<RiceVariant>>>(Resource.Loading())
    val riceVariantsState: StateFlow<Resource<List<RiceVariant>>> = _riceVariantsState.asStateFlow()

    // Trạng thái Khi Thêm Farm
    private val _addFarmState = MutableStateFlow<Resource<String>?>(null)
    val addFarmState: StateFlow<Resource<String>?> = _addFarmState.asStateFlow()

    init {
        loadFarms()
        loadRiceVariants()
    }

    fun loadRiceVariants() {
        viewModelScope.launch {
            masterRepository.getRiceVariants().collect { result ->
                _riceVariantsState.value = result
            }
        }
    }

    fun loadFarms() {
        viewModelScope.launch {
            repository.getFarms(currentUserId).collect { result ->
                _farmsState.value = result
            }
        }
    }

    fun addFarm(
        farmName: String,
        varietyName: String,
        areaM2: String,
        latitude: Double? = null,
        longitude: Double? = null,
        sowingDate: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
    ) {
        if (farmName.isBlank() || varietyName.isBlank() || areaM2.isBlank()) {
            _addFarmState.value = Resource.Error("Vui lòng điền đủ thông tin thửa ruộng!")
            return
        }

        val area = areaM2.toDoubleOrNull()
        if (area == null || area <= 0) {
            _addFarmState.value = Resource.Error("Diện tích phải là một số lớn hơn 0!")
            return
        }

        if (latitude == null || longitude == null) {
            _addFarmState.value = Resource.Error("Vui lòng chọn vị trí trên bản đồ!")
            return
        }

        viewModelScope.launch {
            val newFarm = Farm(
                userId = currentUserId,
                farmName = farmName,
                varietyId = varietyName,
                areaM2 = area,
                location = mapOf("latitude" to latitude, "longitude" to longitude),
                ageDays = 0,
                sowingDate = sowingDate,
                totalGrowthDays = 95
            )

            repository.addFarm(newFarm).collect { result ->
                _addFarmState.value = result
            }
        }
    }

    fun updateSowingDate(farm: Farm, newDate: com.google.firebase.Timestamp) {
        viewModelScope.launch {
            val updatedFarm = farm.copy(sowingDate = newDate)
            repository.updateFarm(updatedFarm).collect { /* Xử lý nếu cần */ }
        }
    }

    // Xoá trạng thái lưu lại màn hình Thêm để tránh popup liên tục
    fun resetAddFarmState() {
        _addFarmState.value = null
    }
}
