package com.example.angrismart.data.repository

import com.example.angrismart.domain.model.FinancialTransaction
import com.example.angrismart.domain.repository.FinancialTransactionRepository
import com.example.angrismart.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FinancialTransactionRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FinancialTransactionRepository {

    override fun getTransactionsByField(fieldId: String): Flow<Resource<List<FinancialTransaction>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection("financial_transactions")
            .whereEqualTo("field_id", fieldId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Lỗi csdl khi tải dữ liệu"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val transactions = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FinancialTransaction::class.java)
                    }
                    trySend(Resource.Success(transactions))
                } else {
                    trySend(Resource.Success(emptyList()))
                }
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun addTransaction(transaction: FinancialTransaction): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val docRef = firestore.collection("financial_transactions").document()
            val newTransaction = transaction.copy(id = docRef.id)
            docRef.set(newTransaction).await()
            emit(Resource.Success(docRef.id))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Có lỗi khi lưu giao dịch!"))
        }
    }
}
