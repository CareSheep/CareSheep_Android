package com.swu.caresheep.ui.guardian.calendar

import java.util.*

data class GuardianSchedule(
    var eventId: String,
    var startTime: Date,
    var endTime: Date,
    var title: String,
    var notification: String,
    var repeat: String,
    var memo: String?
)