package com.example.angrismart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.data.repository.HarvestRepositoryImpl
import com.example.angrismart.domain.model.Harvest
import com.example.angrismart.domain.model.RiceVariant
import com.example.angrismart.domain.model.SeasonTemplate
import com.example.angrismart.domain.repository.HarvestRepository
import com.example.angrismart.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HarvestViewModel(
    private val repository: HarvestRepository = HarvestRepositoryImpl(),
    private val masterRepository: com.example.angrismart.domain.repository.MasterDataRepository = com.example.angrismart.data.repository.MasterDataRepositoryImpl()
) : ViewModel() {

    // Lấy userId thật từ Firebase Auth, fallback "" nếu chưa đăng nhập
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // --- Trạng thái thêm thu hoạch ---
    private val _addHarvestState = MutableStateFlow<Resource<String>?>(null)
    val addHarvestState: StateFlow<Resource<String>?> = _addHarvestState.asStateFlow()

    // --- Danh sách thu hoạch của user ---
    private val _harvestListState = MutableStateFlow<Resource<List<Harvest>>>(Resource.Loading())
    val harvestListState: StateFlow<Resource<List<Harvest>>> = _harvestListState.asStateFlow()

    // --- Danh sách Master Data (để map tên) ---
    private val _riceVariantsState = MutableStateFlow<Resource<List<RiceVariant>>>(Resource.Loading())
    val riceVariantsState: StateFlow<Resource<List<RiceVariant>>> = _riceVariantsState.asStateFlow()

    private val _seasonTemplatesState = MutableStateFlow<Resource<List<SeasonTemplate>>>(Resource.Loading())
    val seasonTemplatesState: StateFlow<Resource<List<SeasonTemplate>>> = _seasonTemplatesState.asStateFlow()

    init {
        loadRiceVariants()
        loadSeasonTemplates()
    }

    fun loadRiceVariants() {
        viewModelScope.launch {
            masterRepository.getRiceVariants().collect { _riceVariantsState.value = it }
        }
    }

    fun loadSeasonTemplates() {
        viewModelScope.launch {
            masterRepository.getSeasonTemplates().collect { _seasonTemplatesState.value = it }
        }
    }

    // --- Danh sách thu hoạch theo ruộng (FieldDetailScreen) ---
    private val _fieldHarvestState = MutableStateFlow<Resource<List<Harvest>>>(Resource.Loading())
    val fieldHarvestState: StateFlow<Resource<List<Harvest>>> = _fieldHarvestState.asStateFlow()

    /** Load toàn bộ lịch sử thu hoạch của người dùng hiện tại */
    fun loadHarvestsByUser() {
        viewModelScope.launch {
            repository.getHarvestsByUser(currentUserId).collect { result ->
                _harvestListState.value = result
            }
        }
    }

    /** Load thu hoạch cho một ruộng cụ thể — chỉ lấy dữ liệu của người dùng hiện tại */
    fun loadHarvestsByField(fieldId: String) {
        viewModelScope.launch {
            repository.getHarvestsByField(fieldId, currentUserId).collect { result ->
                _fieldHarvestState.value = result
            }
        }
    }

    /**
     * Ghi nhận một vụ thu hoạch mới.
     *
     * Tự tính:
     *   totalRevenue = totalWeight × salePrice
     *   profit       = totalRevenue - totalExpense
     */
    fun addHarvest(
        fieldId: String,
        variantName: String,
        totalWeightStr: String,
        salePriceStr: String,
        totalExpenseStr: String,
        cropSeason: String
    ) {
        // --- Validation ---
        if (fieldId.isBlank() || variantName.isBlank() || cropSeason.isBlank()) {
            _addHarvestState.value = Resource.Error("Vui lòng điền đủ thông tin!")
            return
        }

        val totalWeight = totalWeightStr.toDoubleOrNull()
        if (totalWeight == null || totalWeight <= 0) {
            _addHarvestState.value = Resource.Error("Tổng cân nặng phải là số lớn hơn 0!")
            return
        }

        val salePrice = salePriceStr.toDoubleOrNull()
        if (salePrice == null || salePrice <= 0) {
            _addHarvestState.value = Resource.Error("Giá bán phải là số lớn hơn 0!")
            return
        }

        val totalExpense = totalExpenseStr.toDoubleOrNull()
        if (totalExpense == null || totalExpense < 0) {
            _addHarvestState.value = Resource.Error("Tổng chi phí không hợp lệ!")
            return
        }

        // --- Tính toán tài chính ---
        val totalRevenue = totalWeight * salePrice
        val profit = totalRevenue - totalExpense

        val harvest = Harvest(
            fieldId = fieldId,
            userUid = currentUserId,
            variantId = variantName,
            totalWeight = totalWeight,
            salePrice = salePrice,
            totalExpense = totalExpense,
            totalRevenue = totalRevenue,
            profit = profit,
            harvestDate = Timestamp.now(),
            seasonId = cropSeason
        )

        viewModelScope.launch {
            repository.addHarvest(harvest).collect { result ->
                _addHarvestState.value = result
            }
        }
    }

    /** Reset trạng thái sau khi xử lý xong (tránh re-trigger) */
    fun resetAddHarvestState() {
        _addHarvestState.value = null
    }
}
