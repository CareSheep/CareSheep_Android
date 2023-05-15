package com.swu.caresheep.ui.guardian.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianElderRoutineBinding

class GuardianElderRoutineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianElderRoutineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianElderRoutineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.slide_in_right, R.anim.none)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
    }
}