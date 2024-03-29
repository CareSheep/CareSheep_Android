package com.swu.caresheep.guardian.routine.start

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.guardian.routine.walk.GuardianSetWalkTimeActivity
import kotlinx.android.synthetic.main.activity_guardian_start.startSettingButton

class GuardianStartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_start)

        startSettingButton.setOnClickListener {
            startActivity(Intent(this, GuardianSetWalkTimeActivity::class.java))
        }
    }
}