package com.swu.caresheep.ui.elder.connect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.databinding.ActivityElderConnectBinding

class ElderConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityElderConnectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElderConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}