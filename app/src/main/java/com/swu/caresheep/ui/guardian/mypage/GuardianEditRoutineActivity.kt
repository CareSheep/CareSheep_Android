package com.swu.caresheep.ui.guardian.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.databinding.ActivityGuardianEditRoutineBinding

class GuardianEditRoutineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianEditRoutineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianEditRoutineBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}