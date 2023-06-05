package com.swu.caresheep.elder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.ui.elder.main.ElderActivity
import kotlinx.android.synthetic.main.activity_elder_walk_main.walk_no
import kotlinx.android.synthetic.main.activity_elder_walk_main.walk_yes


class ElderWalkMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elder_walk_main)

        // '네' 버튼 => 만보기 화면
        walk_yes.setOnClickListener {
            val intent = Intent(this, ElderWalkActivity::class.java)
            startActivity(intent)
        }
        // '아니오' 버튼 => 홈 화면
        walk_no.setOnClickListener {
            finish()  // finish()로 액티비티를 끝내서 홈으로 돌아가야 함
        }
    }
}