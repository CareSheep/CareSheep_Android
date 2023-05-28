package com.swu.caresheep.ui.guardian.calendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianScheduleDetailBinding

class GuardianScheduleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianScheduleDetailBinding

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로 가기 클릭 시 실행시킬 코드 입력
            finish()
            overridePendingTransition(R.anim.none, R.anim.slide_out_right)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianScheduleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)



    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        // 뒤로 가기
        binding.ivBack.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
    }
}