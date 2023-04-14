package com.swu.caresheep.ui.start

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.swu.caresheep.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var splashScreen : SplashScreen
    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen = installSplashScreen()
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 임시 이동
        binding.btnGoogleSignIn.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }
}