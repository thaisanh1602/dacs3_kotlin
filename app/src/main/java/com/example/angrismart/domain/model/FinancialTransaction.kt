package com.example.angrismart.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class FinancialTransaction(
    val id: String = "",
    
    @get:PropertyName("field_id")
    @set:PropertyName("field_id")
    var fieldId: String = "",
    
    @get:PropertyName("user_uid")
    @set:PropertyName("user_uid")
    var userUid: String = "",
    
    var type: String = "", // 'expense' (chi) hoặc 'income' (thu)
    
    var category: String = "",
    
    var price: Double = 0.0,
    
    var note: String = "",
    
    var date: Timestamp? = null
)
