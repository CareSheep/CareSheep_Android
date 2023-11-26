package com.swu.caresheep.guardian.routine.medicine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_medicine_setting_manual.*

class MedicineSettingManualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_setting_manual)


        medicine_manual_button.setOnClickListener{
            // 다음 액티비티로 이동
            startActivity(Intent(this, GuardianStartSetMedicineActivity::class.java))
        }
    }
}