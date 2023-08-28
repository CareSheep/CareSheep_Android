package com.swu.caresheep.data.model

data class ElderTodaySchedule(
    var time: String,
    var type: Int,  // 0이면 시간, 1이면 종일
    var title: String
)