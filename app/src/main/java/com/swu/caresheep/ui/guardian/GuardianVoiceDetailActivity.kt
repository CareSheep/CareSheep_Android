package com.swu.caresheep.ui.guardian

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.Voice
import kotlinx.android.synthetic.main.activity_guardian_voice_detail.*

class GuardianVoiceDetailActivity : AppCompatActivity() {
    lateinit var data : Voice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_voice_detail)

        // putExtra 메서드로 전달한 데이터 받기 & null일 경우 default 설정
        val check = intent.getIntExtra("check", 0)
        val content = intent.getStringExtra("content")
        val danger = intent.getStringExtra("danger")
        val recording_date = intent.getStringExtra("recording_date")
        val in_need = intent.getStringExtra("in_need")
        val user_id = intent.getIntExtra("user_id", 0)
        val voice_id = intent.getIntExtra("voice_id", 0)

        // 받은 데이터 사용하기
        record_time.text = recording_date
        record_content.text = content

        if(danger == "1") { // 위험 상황일 경우
            record_content.setBackgroundResource(R.color.red) // 버튼 배경 색상을 빨간색으로
        }
        else if(in_need == "1") { // 물건 필요 상황일 경우
            record_content.setBackgroundResource(R.color.blue) // 버튼 배경 색상을 파랑색으로
        }

    }


}