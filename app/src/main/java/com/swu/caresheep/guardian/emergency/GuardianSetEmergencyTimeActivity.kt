package com.swu.caresheep.guardian.emergency

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.guardian.routine.medicine.GuardianStartSetMedicineActivity
import com.swu.caresheep.guardian.routine.medicine.MedicineSettingManualActivity
import com.swu.caresheep.guardian.routine.walk.routine_id
import kotlinx.android.synthetic.main.activity_guardian_set_emergency_time.sleeptime_next_button
import kotlinx.android.synthetic.main.activity_guardian_set_emergency_time.time_count_text
import kotlinx.android.synthetic.main.activity_guardian_set_emergency_time.time_minus_button
import kotlinx.android.synthetic.main.activity_guardian_set_emergency_time.time_plus_button


class GuardianSetEmergencyTimeActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    var time_counter : Int = 0 // 증감할 숫자

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_emergency_time)

        time_plus_button.setOnClickListener {
            time_counter++
            time_count_text.text = time_counter.toString()
        }
        time_minus_button.setOnClickListener {
            time_counter--
            time_count_text.text = time_counter.toString()
        }

        sleeptime_next_button.setOnClickListener {

            // 정수형으로 변환
            val emergencyTimeText = time_count_text.text.toString()
            val time = emergencyTimeText.toInt()

            val updatedData = HashMap<String, Any>()
            updatedData["emergency_time"] = time

            dbRef = FirebaseDatabase.getInstance(BuildConfig.DB_URL).getReference("UsersRoutine").child("$routine_id")
            dbRef.updateChildren(updatedData).addOnSuccessListener(){
                println("Data updated successfully")
            }.addOnFailureListener {
                println("Error updating data: $it")
            }

            // 다음 액티비티 이동
            startActivity(Intent(this, MedicineSettingManualActivity::class.java))

        }
    }
}