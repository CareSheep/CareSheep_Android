package com.swu.caresheep.ui.guardian

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TimePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_breakfast_time.setBreakfastTimeButton
import kotlinx.android.synthetic.main.activity_guardian_set_breakfast_time.timePicker3
import java.util.Objects

class GuardianSetBreakfastTimeActivity : AppCompatActivity() {

    private lateinit var dbRef1: DatabaseReference
    private lateinit var dbRef2: DatabaseReference

    private lateinit var picker: TimePicker
    private lateinit var childUpdates : HashMap<String, Objects>

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

            // 데이터베이스에 데이터 삽입
            val data = hashMapOf(
                "breakfast" to "",
                "dinner" to "",
                "id" to "",
                "lunch" to "",
                "walk" to ""
            )

            dbRef1 = FirebaseDatabase.getInstance().getReference("UsersRoutine")
            dbRef1.push().setValue("2")

            dbRef2 = FirebaseDatabase.getInstance().getReference("UsersRoutine").child("2")
            dbRef2.setValue(data)

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetMedicineNameActivity::class.java))


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

//        childUpdates.put("/User_info/" + ID, userValue)
//        dbRef.updateChildren(childUpdates)
//        dbRef.push().setValue()

    }
}