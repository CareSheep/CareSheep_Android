package com.swu.caresheep

// 파이어베이스에 저장하기 위한 data
data class Voice(
    var check: Int = 0,     // 확인했는지 유무
    var content: String = "",       // 텍스트 내용
    var danger: Int = 0,    // 위험 유무
    var recording_date: String = "",          // 날짜 (음성인식을 실행한) ;초단위라서 고유 => PK
    var in_need: Int = 0,   // 생필품 도움 요청 유무
    var user_id: Int = 1,         // 사용자 id => FK
    var voice_id : Int = 1
)