package com.example.angrismart.utils

import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DataSeeder {
    private val db = FirebaseFirestore.getInstance()

    fun resetData(onComplete: () -> Unit = {}) {
        val collections = listOf(
            "users", "rice_variants", "growth_stages", "Fields",
            "financial_transactions", "Harvests", "season_templates"
        )

        var completedCount = 0
        collections.forEach { collectionName ->
            db.collection(collectionName).get().addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().addOnCompleteListener {
                    completedCount++
                    if (completedCount == collections.size) {
                        Log.d("DataSeeder", "All data reset")
                        onComplete()
                    }
                }
            }.addOnFailureListener {
                completedCount++
                if (completedCount == collections.size) onComplete()
            }
        }
    }

    fun seedData(uid: String? = null) {
        val currentUid = uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: "1"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        // 1. Users
        val usersData = mapOf(
            "id" to "1",
            "name" to "Nguyễn Văn An",
            "email" to "annguyen@gmail.com",
            "password" to "123456",
            "created_at" to dateFormat.parse("2026-04-16T10:00:00Z")?.let { Timestamp(it) }
        )
        db.collection("users").document("1").set(usersData)

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
            mapOf("id" to "s1", "variant_id" to "1", "stage_name" to "Giai đoạn Mạ", "start_day" to 1, "end_day" to 15),
            mapOf("id" to "s2", "variant_id" to "1", "stage_name" to "Giai đoạn Đẻ Nhánh", "start_day" to 16, "end_day" to 40),
            mapOf("id" to "s3", "variant_id" to "1", "stage_name" to "Giai đoạn Làm Đòng", "start_day" to 41, "end_day" to 65),
            mapOf("id" to "s4", "variant_id" to "1", "stage_name" to "Giai đoạn Trổ Bông", "start_day" to 66, "end_day" to 80),
            mapOf("id" to "s5", "variant_id" to "1", "stage_name" to "Giai đoạn Chín", "start_day" to 81, "end_day" to 105),
            mapOf("id" to "s6", "variant_id" to "2", "stage_name" to "Giai đoạn Mạ", "start_day" to 1, "end_day" to 14),
            mapOf("id" to "s7", "variant_id" to "2", "stage_name" to "Giai đoạn Đẻ Nhánh", "start_day" to 15, "end_day" to 35),
            mapOf("id" to "s8", "variant_id" to "2", "stage_name" to "Giai đoạn Làm Đòng", "start_day" to 36, "end_day" to 58),
            mapOf("id" to "s9", "variant_id" to "2", "stage_name" to "Giai đoạn Trổ Bông", "start_day" to 59, "end_day" to 72),
            mapOf("id" to "s10", "variant_id" to "2", "stage_name" to "Giai đoạn Chín", "start_day" to 73, "end_day" to 95),
            mapOf("id" to "s11", "variant_id" to "3", "stage_name" to "Giai đoạn Mạ", "start_day" to 1, "end_day" to 15),
            mapOf("id" to "s12", "variant_id" to "3", "stage_name" to "Giai đoạn Đẻ Nhánh", "start_day" to 16, "end_day" to 38),
            mapOf("id" to "s13", "variant_id" to "3", "stage_name" to "Giai đoạn Làm Đòng", "start_day" to 39, "end_day" to 62),
            mapOf("id" to "s14", "variant_id" to "3", "stage_name" to "Giai đoạn Trổ Bông", "start_day" to 63, "end_day" to 78),
            mapOf("id" to "s15", "variant_id" to "3", "stage_name" to "Giai đoạn Chín", "start_day" to 79, "end_day" to 100),
            mapOf("id" to "s16", "variant_id" to "4", "stage_name" to "Giai đoạn Mạ", "start_day" to 1, "end_day" to 14),
            mapOf("id" to "s17", "variant_id" to "4", "stage_name" to "Giai đoạn Đẻ Nhánh", "start_day" to 15, "end_day" to 34),
            mapOf("id" to "s18", "variant_id" to "4", "stage_name" to "Giai đoạn Làm Đòng", "start_day" to 35, "end_day" to 56),
            mapOf("id" to "s19", "variant_id" to "4", "stage_name" to "Giai đoạn Trổ Bông", "start_day" to 57, "end_day" to 70),
            mapOf("id" to "s20", "variant_id" to "4", "stage_name" to "Giai đoạn Chín", "start_day" to 71, "end_day" to 95)
        )
        growthStages.forEach { stage ->
            db.collection("growth_stages").document(stage["id"].toString()).set(stage)
        }

        // 4. Fields (8 Fields total)
        val fields = listOf(
            // Active Fields
            mapOf("id" to "1", "user_uid" to currentUid, "field_name" to "Ruộng Quế An 01", "area" to 5000.0, "location" to mapOf("latitude" to 15.629603, "longitude" to 108.215403), "current_rice_variant_id" to "1", "sowing_date" to dateFormat.parse("2026-05-01T07:00:00Z")?.let { Timestamp(it) }, "status" to "active", "is_harvested" to 0),
            mapOf("id" to "2", "user_uid" to currentUid, "field_name" to "Ruộng Ba Làng 02", "area" to 7500.0, "location" to mapOf("latitude" to 15.635000, "longitude" to 108.220000), "current_rice_variant_id" to "2", "sowing_date" to dateFormat.parse("2026-04-15T07:00:00Z")?.let { Timestamp(it) }, "status" to "active", "is_harvested" to 0),
            mapOf("id" to "3", "user_uid" to currentUid, "field_name" to "Ruộng Đồng Cát 03", "area" to 3000.0, "location" to mapOf("latitude" to 15.640000, "longitude" to 108.225000), "current_rice_variant_id" to "3", "sowing_date" to dateFormat.parse("2026-03-25T07:00:00Z")?.let { Timestamp(it) }, "status" to "active", "is_harvested" to 0),
            mapOf("id" to "4", "user_uid" to currentUid, "field_name" to "Ruộng Ven Sông 04", "area" to 10000.0, "location" to mapOf("latitude" to 15.645000, "longitude" to 108.230000), "current_rice_variant_id" to "4", "sowing_date" to dateFormat.parse("2026-03-05T07:00:00Z")?.let { Timestamp(it) }, "status" to "active", "is_harvested" to 0),
            // Harvested Fields (Sown in Jan/Feb)
            mapOf("id" to "5", "user_uid" to currentUid, "field_name" to "Ruộng Bàu Chát 05", "area" to 6000.0, "location" to mapOf("latitude" to 15.650000, "longitude" to 108.235000), "current_rice_variant_id" to "1", "sowing_date" to dateFormat.parse("2026-01-10T07:00:00Z")?.let { Timestamp(it) }, "status" to "harvested", "is_harvested" to 1),
            mapOf("id" to "6", "user_uid" to currentUid, "field_name" to "Ruộng Gò Nổi 06", "area" to 4500.0, "location" to mapOf("latitude" to 15.655000, "longitude" to 108.240000), "current_rice_variant_id" to "2", "sowing_date" to dateFormat.parse("2026-01-20T07:00:00Z")?.let { Timestamp(it) }, "status" to "harvested", "is_harvested" to 1),
            mapOf("id" to "7", "user_uid" to currentUid, "field_name" to "Ruộng Đồng Đế 07", "area" to 8000.0, "location" to mapOf("latitude" to 15.660000, "longitude" to 108.245000), "current_rice_variant_id" to "3", "sowing_date" to dateFormat.parse("2026-01-15T07:00:00Z")?.let { Timestamp(it) }, "status" to "harvested", "is_harvested" to 1),
            mapOf("id" to "8", "user_uid" to currentUid, "field_name" to "Ruộng Cửa Đại 08", "area" to 12000.0, "location" to mapOf("latitude" to 15.665000, "longitude" to 108.250000), "current_rice_variant_id" to "4", "sowing_date" to dateFormat.parse("2026-02-01T07:00:00Z")?.let { Timestamp(it) }, "status" to "harvested", "is_harvested" to 1)
        )
        fields.forEach { field ->
            db.collection("Fields").document(field["id"].toString()).set(field)
        }

        // 5. Financial Transactions (24 Transactions)
        val transactions = listOf(
            mapOf("id" to "t1", "field_id" to "1", "user_uid" to currentUid, "type" to "expense", "category" to "Phân bón", "price" to 200000.0, "note" to "Phân NPK đợt 1", "date" to dateFormat.parse("2026-05-02T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t2", "field_id" to "1", "user_uid" to currentUid, "type" to "expense", "category" to "Thuốc trừ sâu", "price" to 100000.0, "note" to "Trị rầy nâu", "date" to dateFormat.parse("2026-05-10T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t3", "field_id" to "1", "user_uid" to currentUid, "type" to "expense", "category" to "Công làm cỏ", "price" to 50000.0, "note" to "Thuê nhân công", "date" to dateFormat.parse("2026-05-12T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t4", "field_id" to "2", "user_uid" to currentUid, "type" to "expense", "category" to "Phân bón", "price" to 200000.0, "note" to "Lân và Kali", "date" to dateFormat.parse("2026-04-20T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t5", "field_id" to "2", "user_uid" to currentUid, "type" to "expense", "category" to "Giống lúa", "price" to 200000.0, "note" to "Mua giống Jasmine", "date" to dateFormat.parse("2026-04-15T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t6", "field_id" to "2", "user_uid" to currentUid, "type" to "expense", "category" to "Xăng dầu", "price" to 100000.0, "note" to "Máy bơm nước", "date" to dateFormat.parse("2026-05-01T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t7", "field_id" to "3", "user_uid" to currentUid, "type" to "expense", "category" to "Thuốc bảo vệ", "price" to 100000.0, "note" to "Thuốc dưỡng bông", "date" to dateFormat.parse("2026-04-10T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t8", "field_id" to "3", "user_uid" to currentUid, "type" to "expense", "category" to "Phân bón", "price" to 100000.0, "note" to "Ure", "date" to dateFormat.parse("2026-04-01T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t9", "field_id" to "3", "user_uid" to currentUid, "type" to "expense", "category" to "Máy cày", "price" to 100000.0, "note" to "Thuê cày đất", "date" to dateFormat.parse("2026-03-25T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t10", "field_id" to "4", "user_uid" to currentUid, "type" to "expense", "category" to "Phân bón", "price" to 300000.0, "note" to "Hỗn hợp lân kali", "date" to dateFormat.parse("2026-03-10T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t11", "field_id" to "4", "user_uid" to currentUid, "type" to "expense", "category" to "Công gặt", "price" to 300000.0, "note" to "Thuê máy gặt", "date" to dateFormat.parse("2026-03-20T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t12", "field_id" to "4", "user_uid" to currentUid, "type" to "expense", "category" to "Vận chuyển", "price" to 200000.0, "note" to "Chở lúa về kho", "date" to dateFormat.parse("2026-03-22T10:00:00Z")?.let { Timestamp(it) }),
            // Early Transactions for fields 5-8 (Jan/Feb)
            mapOf("id" to "t13", "field_id" to "5", "user_uid" to currentUid, "type" to "expense", "category" to "Phân bón", "price" to 150000.0, "note" to "Lót giống", "date" to dateFormat.parse("2026-01-15T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t14", "field_id" to "5", "user_uid" to currentUid, "type" to "expense", "category" to "Thuốc sâu", "price" to 80000.0, "note" to "Đợt đầu", "date" to dateFormat.parse("2026-02-10T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t15", "field_id" to "5", "user_uid" to currentUid, "type" to "expense", "category" to "Nước tưới", "price" to 50000.0, "note" to "Phí mương", "date" to dateFormat.parse("2026-03-01T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t16", "field_id" to "6", "user_uid" to currentUid, "type" to "expense", "category" to "Phân hữu cơ", "price" to 300000.0, "note" to "Bón lót", "date" to dateFormat.parse("2026-01-25T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t17", "field_id" to "6", "user_uid" to currentUid, "type" to "expense", "category" to "Làm đất", "price" to 200000.0, "note" to "Cày ải", "date" to dateFormat.parse("2026-01-20T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t18", "field_id" to "6", "user_uid" to currentUid, "type" to "expense", "category" to "Giống", "price" to 150000.0, "note" to "Jasmine giống", "date" to dateFormat.parse("2026-01-22T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t19", "field_id" to "7", "user_uid" to currentUid, "type" to "expense", "category" to "Thuê máy", "price" to 500000.0, "note" to "Máy gặt", "date" to dateFormat.parse("2026-04-20T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t20", "field_id" to "7", "user_uid" to currentUid, "type" to "expense", "category" to "Bao bì", "price" to 100000.0, "note" to "Đựng lúa", "date" to dateFormat.parse("2026-04-25T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t21", "field_id" to "7", "user_uid" to currentUid, "type" to "expense", "category" to "Bốc vác", "price" to 100000.0, "note" to "Vận chuyển", "date" to dateFormat.parse("2026-04-28T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t22", "field_id" to "8", "user_uid" to currentUid, "type" to "expense", "category" to "Phân bón", "price" to 400000.0, "note" to "Tổng hợp", "date" to dateFormat.parse("2026-02-15T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t23", "field_id" to "8", "user_uid" to currentUid, "type" to "expense", "category" to "Công nhật", "price" to 300000.0, "note" to "Làm cỏ", "date" to dateFormat.parse("2026-03-01T10:00:00Z")?.let { Timestamp(it) }),
            mapOf("id" to "t24", "field_id" to "8", "user_uid" to currentUid, "type" to "expense", "category" to "Bơm thuốc", "price" to 200000.0, "note" to "Trừ sâu", "date" to dateFormat.parse("2026-03-15T10:00:00Z")?.let { Timestamp(it) })
        )
        transactions.forEach { trans ->
            db.collection("financial_transactions").document(trans["id"].toString()).set(trans)
        }

        // 6. Harvests (4 Harvests for Fields 5, 6, 7, 8)
        val harvests = listOf(
            mapOf("id" to "h5", "field_id" to "5", "user_uid" to currentUid, "total_weight" to 450.0, "rice_variant_id" to "1", "sale_price" to 8500.0, "total_expense" to 280000.0, "total_revenue" to 3825000.0, "profit" to 3545000.0, "harvest_date" to dateFormat.parse("2026-05-12T08:00:00Z")?.let { Timestamp(it) }, "season_template_id" to "1", "is_harvested" to "0"),
            mapOf("id" to "h6", "field_id" to "6", "user_uid" to currentUid, "total_weight" to 400.0, "rice_variant_id" to "2", "sale_price" to 8300.0, "total_expense" to 650000.0, "total_revenue" to 3320000.0, "profit" to 2670000.0, "harvest_date" to dateFormat.parse("2026-05-01T08:00:00Z")?.let { Timestamp(it) }, "season_template_id" to "1", "is_harvested" to "0"),
            mapOf("id" to "h7", "field_id" to "7", "user_uid" to currentUid, "total_weight" to 600.0, "rice_variant_id" to "3", "sale_price" to 8800.0, "total_expense" to 700000.0, "total_revenue" to 5280000.0, "profit" to 4580000.0, "harvest_date" to dateFormat.parse("2026-05-08T08:00:00Z")?.let { Timestamp(it) }, "season_template_id" to "2", "is_harvested" to "0"),
            mapOf("id" to "h8", "field_id" to "8", "user_uid" to currentUid, "total_weight" to 900.0, "rice_variant_id" to "4", "sale_price" to 8100.0, "total_expense" to 900000.0, "total_revenue" to 7290000.0, "profit" to 6390000.0, "harvest_date" to dateFormat.parse("2026-05-11T08:00:00Z")?.let { Timestamp(it) }, "season_template_id" to "2", "is_harvested" to "0")
        )
        harvests.forEach { harvest ->
            db.collection("Harvests").document(harvest["id"].toString()).set(harvest)
        }

        // 7. Season Templates
        val seasonTemplates = listOf(
            mapOf("id" to "1", "season_name" to "Đông Xuân", "start_month" to 12, "start_day" to 1, "end_month" to 4, "end_day" to 30),
            mapOf("id" to "2", "season_name" to "Hè Thu", "start_month" to 5, "start_day" to 1, "end_month" to 8, "end_day" to 31),
            mapOf("id" to "3", "season_name" to "Thu Đông", "start_month" to 9, "start_day" to 1, "end_month" to 11, "end_day" to 30)
        )
        seasonTemplates.forEach { template ->
            db.collection("season_templates").document(template["id"].toString()).set(template)
        }

        Log.d("DataSeeder", "Economy data with 4 harvested fields seeded")
    }
}
