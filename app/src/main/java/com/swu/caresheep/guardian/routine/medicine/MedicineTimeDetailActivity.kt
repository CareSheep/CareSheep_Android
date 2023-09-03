package com.swu.caresheep.guardian.routine.medicine

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_dinner_time.dinnerTimePicker
import kotlinx.android.synthetic.main.activity_medicine_time_detail.medicine_timePicker
import kotlinx.android.synthetic.main.activity_medicine_time_detail.selected_button

class MedicineTimeDetailActivity : AppCompatActivity() {

    private lateinit var picker: TimePicker
    var timehour: Int = 0
    var timeminute: Int = 0
    var am_pm: String = "am"

    var result:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_time_detail)

        selected_button.setOnClickListener {
            // timepicker에서 시간 가져오기
            pushTime()

            Intent(this , MedicineTimePickerListAdapter::class.java).apply {
                // 데이터 자체를 전달
                putExtra("time", result)

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }
        }
    }

    private fun pushTime() {
        dinnerTimePicker.is24HourView()
        picker = medicine_timePicker

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