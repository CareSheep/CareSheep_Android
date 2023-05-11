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
        val danger = intent.getIntExtra("danger", 0)
        val recording_date = intent.getStringExtra("recording_date")
        val in_need = intent.getIntExtra("in_need", 0)
        val user_id = intent.getIntExtra("user_id", 0)
        val voice_id = intent.getIntExtra("voice_id", 0)

        // 받은 데이터 사용하기
        record_time.text = recording_date
        record_content.text = content

    }


}