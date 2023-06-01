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
import com.swu.caresheep.ui.guardian.GuardianStartSetMedicineActivity
import com.swu.caresheep.ui.guardian.routine_id
import kotlinx.android.synthetic.main.activity_guardian_set_dinner_time.dinnerTimePicker
import kotlinx.android.synthetic.main.activity_guardian_set_dinner_time.setDinnerTimeButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class GuardianSetDinnerTimeActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference

    private lateinit var picker: TimePicker

    var timehour: Int = 0
    var timeminute: Int = 0
    var am_pm: String = "am"

    var result : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_dinner_time)

        setDinnerTimeButton.setOnClickListener {
            //timepicker에서 시간 가져오는 함수
            pushTime()

            // 데이터베이스에 데이터 삽입
            val data = hashMapOf(
                "dinner" to result,
            )

            dbRef = FirebaseDatabase.getInstance().getReference("UsersRoutine").child("$routine_id")

            val updatedData = HashMap<String, Any>()
            updatedData["dinner"] = result

            dbRef.updateChildren(updatedData).addOnSuccessListener(){
                println("Data updated successfully")
            }.addOnFailureListener {
                println("Error updating data: $it")
            }

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianStartSetMedicineActivity::class.java))
        }
    }

    private fun pushTime() {
        picker = dinnerTimePicker

        val hour = picker.hour
        val minute = picker.minute

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