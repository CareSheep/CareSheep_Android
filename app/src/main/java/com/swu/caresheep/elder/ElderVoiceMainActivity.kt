package com.swu.caresheep.elder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_elder_voice_main.*


class ElderVoiceMainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elder_voice_main)

        // '네' 버튼 => 녹음 화면
        voice_yes.setOnClickListener {
            val intent = Intent(this, ElderVoiceSubActivity::class.java)
            startActivity(intent)
        }
        // '아니오' 버튼 => 홈 화면
        voice_no.setOnClickListener {
            finish()
        }

    }

}