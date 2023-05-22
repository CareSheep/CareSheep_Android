package com.swu.caresheep.ui.guardian.meal

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_breakfast_time.setBreakfastTimeButton
import kotlinx.android.synthetic.main.activity_guardian_set_breakfast_time.timePicker3

class GuardianSetBreakfastTimeActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference

    private lateinit var picker: TimePicker

    var timehour: Int = 0
    var timeminute: Int = 0
    var am_pm: String = "am"

    var result:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_breakfast_time)

        setBreakfastTimeButton.setOnClickListener {
            //timepicker에서 시간 가져오는 함수
            pushTime()

            dbRef = FirebaseDatabase.getInstance().getReference("UsersRoutine").child("test")

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