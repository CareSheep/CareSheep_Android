package com.swu.caresheep.recyclerview

data class RecordData(
    val voice_id : Int,
    val content: String,
    val recording_date: String
) {
    // 인자 없는 생성자 =>, Firebase에서 데이터를 가져올 때 필요한 기본 생성자를 만족시켜주기 위해
    constructor() : this(0, "", "")
}