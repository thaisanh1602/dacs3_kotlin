package com.example.angrismart.domain.repository

import com.example.angrismart.domain.model.FinancialTransaction
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.Flow

interface FinancialTransactionRepository {
    fun getTransactionsByField(fieldId: String): Flow<Resource<List<FinancialTransaction>>>
    suspend fun addTransaction(transaction: FinancialTransaction): Flow<Resource<String>>
}
