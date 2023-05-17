package com.swu.caresheep.ui.guardian

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class GuardianSetedRoutine (
    var id : String,
    var breakfast : String = "",
    var lunch : String ="",
    var dinner : String= "",
    var walk : String=""
){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "breakfast" to breakfast,
            "lunch" to lunch,
            "dinner" to dinner,
            "walk" to walk,
        )
    }
}

