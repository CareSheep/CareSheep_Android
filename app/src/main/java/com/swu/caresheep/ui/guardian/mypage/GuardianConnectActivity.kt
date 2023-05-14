package com.swu.caresheep.ui.guardian.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.databinding.ActivityGuardianConnectBinding

class GuardianConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianConnectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}