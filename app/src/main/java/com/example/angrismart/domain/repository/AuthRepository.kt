package com.example.angrismart.domain.repository

import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun loginWithEmail(email: String, password: String): Flow<Resource<String>>
    suspend fun registerWithEmail(email: String, password: String): Flow<Resource<String>>
    suspend fun sendPasswordResetEmail(email: String): Flow<Resource<String>>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    fun logout()
}
