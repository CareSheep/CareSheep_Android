package com.swu.caresheep.ui.guardian

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.ui.guardian.medicine.GuardianSetMedicineColorActivity
import kotlinx.android.synthetic.main.activity_guardian_start_set_medicine.start_set_medicine_button
import kotlinx.android.synthetic.main.activity_guardian_start_set_medicine.stop_set_medicine_button

class GuardianStartSetMedicineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_start_set_medicine)

        start_set_medicine_button.setOnClickListener {
            // 약 설정 액티비티 이동
            startActivity(Intent(this, GuardianSetMedicineColorActivity::class.java))
            finish()
        }

        stop_set_medicine_button.setOnClickListener {
            // 보호자 첫 액티비티 이동
            finish()
        }
    }
}