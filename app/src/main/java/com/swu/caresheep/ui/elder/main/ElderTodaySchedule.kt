package com.swu.caresheep.ui.elder.main

data class ElderTodaySchedule(
    var time: String,
    var type: Int,  // 0이면 시간, 1이면 종일
    var title: String
)