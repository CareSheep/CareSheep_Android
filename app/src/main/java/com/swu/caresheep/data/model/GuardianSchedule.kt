package com.swu.caresheep.data.model

import com.google.api.client.util.DateTime

data class GuardianSchedule(
    var eventId: String,
    var type: Int,  // 0이면 시간, 1이면 종일
    var startTime: DateTime,
    var endTime: DateTime,
    var title: String,
    var notification: String,
    var repeat: String,
    var memo: String?
)