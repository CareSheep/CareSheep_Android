package com.swu.caresheep.guardian.emergency

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_elder_emergency.back_button
import kotlinx.android.synthetic.main.activity_guardian_elder_emergency.call_number
import kotlinx.android.synthetic.main.activity_guardian_elder_emergency.emergency_next1_button

class GuardianElderEmergencyActivity : AppCompatActivity() {

    lateinit var callNumber : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_elder_emergency)

        emergency_next1_button.setOnClickListener {
            callNumber = call_number.text.toString()
            val intent1 = Intent(Intent.ACTION_DIAL)
            intent1.data = Uri.parse("tel:$callNumber")
            if(intent1.resolveActivity(packageManager) != null){
                startActivity(intent1)
            }
        }

        back_button.setOnClickListener {
            finish()
        }
    }
}