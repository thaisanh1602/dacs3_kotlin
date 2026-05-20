package com.example.angrismart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angrismart.domain.model.FinancialTransaction
import com.example.angrismart.domain.repository.FinancialTransactionRepository
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FinancialTransactionViewModel(
    private val repository: FinancialTransactionRepository = com.example.angrismart.data.repository.FinancialTransactionRepositoryImpl()
) : ViewModel() {

    private val _transactions = MutableStateFlow<Resource<List<FinancialTransaction>>>(Resource.Loading())
    val transactions: StateFlow<Resource<List<FinancialTransaction>>> = _transactions.asStateFlow()

    private val _addTransactionStatus = MutableStateFlow<Resource<String>?>(null)
    val addTransactionStatus: StateFlow<Resource<String>?> = _addTransactionStatus.asStateFlow()

    fun getTransactionsByField(fieldId: String) {
        viewModelScope.launch {
            repository.getTransactionsByField(fieldId).collectLatest { result ->
                _transactions.value = result
            }
        }
    }

    fun addTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction).collectLatest { result ->
                _addTransactionStatus.value = result
            }
        }
    }

    fun resetAddTransactionStatus() {
        _addTransactionStatus.value = null
    }
}
