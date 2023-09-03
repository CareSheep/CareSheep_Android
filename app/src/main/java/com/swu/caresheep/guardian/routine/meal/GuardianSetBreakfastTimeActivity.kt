package com.swu.caresheep.guardian.routine.meal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import com.swu.caresheep.guardian.routine.walk.routine_id
import kotlinx.android.synthetic.main.activity_guardian_set_breakfast_time.setBreakfastTimeButton
import kotlinx.android.synthetic.main.activity_guardian_set_breakfast_time.timePicker3
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GuardianSetBreakfastTimeActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference

    private lateinit var picker: TimePicker

    var timehour: Int = 0
    var timeminute: Int = 0
    var am_pm: String = "am"

    var result : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_breakfast_time)

        setBreakfastTimeButton.setOnClickListener {
            //timepicker에서 시간 가져오는 함수
            pushTime()

            dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("UsersRoutine").child("$routine_id")

            val updatedData = HashMap<String, Any>()
            updatedData["breakfast"] = result

            dbRef.updateChildren(updatedData).addOnSuccessListener(){
                println("Data updated successfully")
            }.addOnFailureListener {
                println("Error updating data: $it")
            }

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetLunchTimeActivity::class.java))
        }
    }

    private fun pushTime() {
        picker = timePicker3

        // TimePicker 변경 시 값 설정
        picker.setOnTimeChangedListener { _, hour, minute ->
            // 시간과 분을 형식화하여 문자열로 변환
            val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }.time
            )
            result = timeString
            Log.d("저장되는 시간은", result)
        }

        // 초기 시간 반영
        val initialHour = picker.hour
        val initialMinute = picker.minute
        val initialTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, initialHour)
                set(Calendar.MINUTE, initialMinute)
            }.time
        )
        result = initialTimeString
        Log.d("저장되는 시간은", result)
    }

}