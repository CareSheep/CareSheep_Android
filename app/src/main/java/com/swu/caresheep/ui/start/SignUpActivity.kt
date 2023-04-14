package com.swu.caresheep.ui.start

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.swu.caresheep.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.ivBack.setOnClickListener {
            this.onBackPressed()
        }
    }
}