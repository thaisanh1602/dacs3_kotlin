package com.example.angrismart.data.repository

import com.example.angrismart.domain.model.RiceVariant
import com.example.angrismart.domain.model.SeasonTemplate
import com.example.angrismart.domain.repository.MasterDataRepository
import com.example.angrismart.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MasterDataRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MasterDataRepository {

    override fun getRiceVariants(): Flow<Resource<List<RiceVariant>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("rice_variants")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Lỗi tải giống lúa"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(RiceVariant::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e("MasterDataRepo", "Error parsing RiceVariant: ${e.message}")
                        null
                    }
                } ?: emptyList()
                trySend(Resource.Success(list))
            }
        awaitClose { subscription.remove() }
    }

    override fun getSeasonTemplates(): Flow<Resource<List<SeasonTemplate>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("season_templates")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Lỗi tải vụ mùa"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(SeasonTemplate::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e("MasterDataRepo", "Error parsing SeasonTemplate: ${e.message}")
                        null
                    }
                } ?: emptyList()
                trySend(Resource.Success(list))
            }
        awaitClose { subscription.remove() }
    }
}
