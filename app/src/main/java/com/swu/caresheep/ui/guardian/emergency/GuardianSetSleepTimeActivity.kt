package com.swu.caresheep.ui.guardian.emergency

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.ui.guardian.GuardianStartSetMedicineActivity
import com.swu.caresheep.ui.guardian.routine_id
import kotlinx.android.synthetic.main.activity_guardian_set_sleep_time.sleepTimeButton
import kotlinx.android.synthetic.main.activity_guardian_set_sleep_time.sleeptimePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// 자는 시간 설정하는 액티비티

class GuardianSetSleepTimeActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference

    private lateinit var picker: TimePicker

    var result : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_sleep_time)

        sleepTimeButton.setOnClickListener{
            //timepicker에서 시간 가져오는 함수
            pushTime()

            // 데이터베이스에 데이터 삽입
            dbRef = FirebaseDatabase.getInstance(BuildConfig.DB_URL).getReference("UsersRoutine").child("$routine_id")

            val updatedData = HashMap<String, Any>()
            updatedData["sleep"] = result

            dbRef.updateChildren(updatedData).addOnSuccessListener(){
                println("Data updated successfully")
            }.addOnFailureListener {
                println("Error updating data: $it")
            }

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetEmergencyTimeActivity::class.java))
        }
    }

    private fun pushTime() {
        picker = sleeptimePicker

        val hour = picker.hour
        val minute = picker.minute

        var am_pm: String = "am"

        var result : String = ""

        // 시간과 분을 형식화하여 문자열로 변환
        val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }.time
        )
        result = timeString
        Log.d("저장되는 시간은","$result")

    }
}