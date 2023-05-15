package com.swu.caresheep.ui.guardian

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class GuardianSetedMedicine (
    var id : String,
    var color : String = "",
    var count : String ="", // 1회 복용시 알약 개수
    var medicine_name : String= "",
    var single_dose : String="",
    var time : String = "",
        ){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "color" to color,
            "count" to count,
            "medicine_name" to medicine_name,
            "time" to time,
        )
    }
}