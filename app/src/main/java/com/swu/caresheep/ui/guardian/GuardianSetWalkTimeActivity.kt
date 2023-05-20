package com.swu.caresheep.ui.guardian

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_walk_time.setWalkTimeButton
import kotlinx.android.synthetic.main.activity_guardian_set_walk_time.timePicker

class GuardianSetWalkTimeActivity : AppCompatActivity() {

    private lateinit var dbRef1: DatabaseReference
    private lateinit var dbRef2: DatabaseReference

    private lateinit var picker: TimePicker

    var timehour: Int = 0
    var timeminute: Int = 0
    var am_pm: String = "am"

    var result:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_walk_time)

        setWalkTimeButton.setOnClickListener {
            pushTime()

            // 데이터베이스에 데이터 삽입
            val data = hashMapOf(
                "breakfast" to "",
                "dinner" to "",
                "id" to 1,
                "lunch" to "",
                "walk_time" to result,
                "walk_step" to 0
            )

            dbRef1 = FirebaseDatabase.getInstance().getReference("UsersRoutine")
            dbRef1.setValue("test") // 추후 user_id로 전환

            dbRef2 = FirebaseDatabase.getInstance().getReference("UsersRoutine").child("test")
            dbRef2.setValue(data)

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetWalkStepActivity::class.java))
        }
    }

    private fun pushTime() {
        picker = timePicker
        // picker.is24HourView = true

        if (Build.VERSION.SDK_INT >= 23) {
            timehour = picker.hour
            timeminute = picker.minute
        } else {
            timehour = picker.currentHour
            timeminute = picker.currentMinute
        }

        if (timehour > 12) {
            am_pm = "PM"
            timehour -= 12
        } else {
            am_pm = "AM"
        }

        result = "$timehour:$timeminute"
        // print(result)
        Log.d("T",result)

    }
}