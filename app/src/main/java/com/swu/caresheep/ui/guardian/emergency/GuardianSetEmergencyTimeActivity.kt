package com.swu.caresheep.ui.guardian.emergency

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.ui.guardian.GuardianStartSetMedicineActivity
import kotlinx.android.synthetic.main.activity_guardian_set_emergency_time.sleeptime_next_button
import kotlinx.android.synthetic.main.activity_guardian_set_emergency_time.time_count_text
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_singledose.single_dose_count_text


class GuardianSetEmergencyTimeActivity : AppCompatActivity() {

    var time_counter : Int = 0 // 증감할 숫자

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_emergency_time)

        time_count_text.setOnClickListener {
            time_counter++
            time_count_text.text = time_counter.toString()
        }
        time_count_text.setOnClickListener {
            time_counter--
            time_count_text.text = time_counter.toString()
        }

        sleeptime_next_button.setOnClickListener {

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianStartSetMedicineActivity::class.java))
        }
    }
}