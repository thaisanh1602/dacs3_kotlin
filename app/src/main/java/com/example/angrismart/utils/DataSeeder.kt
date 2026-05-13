package com.example.angrismart.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DataSeeder {
    private val db = FirebaseFirestore.getInstance()

    fun seedData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        // 1. Users
        val usersData = mapOf(
            "name" to "Nguyễn Văn An",
            "email" to "annguyen@gmail.com",
            "created_at" to dateFormat.parse("2026-04-16T10:00:00Z")?.let { Timestamp(it) }
        )
        db.collection("users").document("1").set(usersData)
            .addOnSuccessListener { Log.d("DataSeeder", "Users seeded") }

        // 2. Rice Variants
        val riceVariants = listOf(
            mapOf("id" to "1", "name" to "ST25", "total_growth_days" to 105),
            mapOf("id" to "2", "name" to "Jasmine 85", "total_growth_days" to 95),
            mapOf("id" to "3", "name" to "Đài Thơm 8", "total_growth_days" to 100),
            mapOf("id" to "4", "name" to "OM5451", "total_growth_days" to 95)
        )
        riceVariants.forEach { variant ->
            db.collection("rice_variants").document(variant["id"].toString()).set(variant)
        }

        // 3. Growth Stages
        val growthStages = listOf(
            mapOf("variant_id" to "1", "stage_name" to "Giai đoạn Mạ", "start_day" to 1, "end_day" to 15),
            mapOf("variant_id" to "1", "stage_name" to "Giai đoạn Đẻ Nhánh", "start_day" to 16, "end_day" to 40),
            mapOf("variant_id" to "1", "stage_name" to "Giai đoạn Làm Đòng", "start_day" to 41, "end_day" to 65),
            mapOf("variant_id" to "1", "stage_name" to "Giai đoạn Trổ Bông", "start_day" to 66, "end_day" to 80),
            mapOf("variant_id" to "1", "stage_name" to "Giai đoạn Chín", "start_day" to 81, "end_day" to 105),

            mapOf("variant_id" to "2", "stage_name" to "Giai đoạn Mạ", "start_day" to 1, "end_day" to 14),
            mapOf("variant_id" to "2", "stage_name" to "Giai đoạn Đẻ Nhánh", "start_day" to 15, "end_day" to 35),
            mapOf("variant_id" to "2", "stage_name" to "Giai đoạn Làm Đòng", "start_day" to 36, "end_day" to 58),
            mapOf("variant_id" to "2", "stage_name" to "Giai đoạn Trổ Bông", "start_day" to 59, "end_day" to 72),
            mapOf("variant_id" to "2", "stage_name" to "Giai đoạn Chín", "start_day" to 73, "end_day" to 95),

            mapOf("variant_id" to "3", "stage_name" to "Giai đoạn Mạ", "start_day" to 1, "end_day" to 15),
            mapOf("variant_id" to "3", "stage_name" to "Giai đoạn Đẻ Nhánh", "start_day" to 16, "end_day" to 38),
            mapOf("variant_id" to "3", "stage_name" to "Giai đoạn Làm Đòng", "start_day" to 39, "end_day" to 62),
            mapOf("variant_id" to "3", "stage_name" to "Giai đoạn Trổ Bông", "start_day" to 63, "end_day" to 78),
            mapOf("variant_id" to "3", "stage_name" to "Giai đoạn Chín", "start_day" to 79, "end_day" to 100),

            mapOf("variant_id" to "4", "stage_name" to "Giai đoạn Mạ", "start_day" to 1, "end_day" to 14),
            mapOf("variant_id" to "4", "stage_name" to "Giai đoạn Đẻ Nhánh", "start_day" to 15, "end_day" to 34),
            mapOf("variant_id" to "4", "stage_name" to "Giai đoạn Làm Đòng", "start_day" to 35, "end_day" to 56),
            mapOf("variant_id" to "4", "stage_name" to "Giai đoạn Trổ Bông", "start_day" to 57, "end_day" to 70),
            mapOf("variant_id" to "4", "stage_name" to "Giai đoạn Chín", "start_day" to 71, "end_day" to 95)
        )
        growthStages.forEach { stage ->
            db.collection("growth_stages").add(stage)
        }

        // 4. Fields
        val fieldsData = mapOf(
            "owner_uid" to "1",
            "field_name" to "Ruộng Quế An 01",
            "area" to 5000,
            "location" to mapOf("latitude" to 15.629603, "longitude" to 108.215403),
            "current_rice_variant_id" to "1",
            "sowing_date" to dateFormat.parse("2026-04-01T07:00:00Z")?.let { Timestamp(it) },
            "status" to "active"
        )
        db.collection("Fields").document("1").set(fieldsData)

        // 5. Financial Transactions
        val transactionData = mapOf(
            "field_id" to "1",
            "user_uid" to "1",
            "type" to "expense",
            "category" to "Mua phân bón",
            "price" to 200000,
            "note" to "Mua 2 bao phân NPK",
            "date" to dateFormat.parse("2026-04-10T15:30:00Z")?.let { Timestamp(it) }
        )
        db.collection("financial_transactions").document("1").set(transactionData)

        // 6. Harvests
        val harvestData = mapOf(
            "field_id" to "1",
            "user_uid" to "1",
            "total_weight" to 350,
            "rice_variant_id" to "3",
            "sale_price" to 8500,
            "total_expense" to 200000,
            "total_revenue" to 2775000,
            "harvest_date" to dateFormat.parse("2026-07-15T08:00:00Z")?.let { Timestamp(it) },
            "season_template_id" to "1"
        )
        db.collection("Harvests").document("1").set(harvestData)

        // 7. Season Templates
        val seasonTemplates = listOf(
            mapOf("id" to "1", "season_name" to "Đông Xuân", "start_month" to 12, "start_day" to 1, "end_month" to 4, "end_day" to 30),
            mapOf("id" to "2", "season_name" to "Hè Thu", "start_month" to 5, "start_day" to 1, "end_month" to 8, "end_day" to 31),
            mapOf("id" to "3", "season_name" to "Thu Đông", "start_month" to 9, "start_day" to 1, "end_month" to 11, "end_day" to 30)
        )
        seasonTemplates.forEach { template ->
            db.collection("season_templates").document(template["id"].toString()).set(template)
        }

        Log.d("DataSeeder", "All dummy data seeded to Firebase")
    }
}
