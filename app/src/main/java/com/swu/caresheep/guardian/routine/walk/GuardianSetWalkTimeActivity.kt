package com.swu.caresheep.guardian.routine.walk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import com.swu.caresheep.start.user_id
import kotlinx.android.synthetic.main.activity_guardian_set_walk_time.setWalkTimeButton
import kotlinx.android.synthetic.main.activity_guardian_set_walk_time.timePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

var routine_id : Int = 0
class GuardianSetWalkTimeActivity : AppCompatActivity() {

    private lateinit var dbRef1: DatabaseReference
    private lateinit var dbRef2: DatabaseReference

    private lateinit var picker: TimePicker

    var timehour: Int = 0
    var timeminute: Int = 0
    var am_pm: String = "am"

    var result : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_walk_time)

        setWalkTimeButton.setOnClickListener {
            pushTime()

            // 데이터베이스에 데이터 삽입
            val data = hashMapOf(
                "breakfast" to "",
                "dinner" to "",
                "user_id" to user_id,
                "lunch" to "",
                "walk_time" to result,
                "walk_step" to 0
            )

            dbRef1 = FirebaseDatabase.getInstance(DB_URL).getReference("UsersRoutine")
            dbRef1.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val childCount = dataSnapshot.childrenCount
                    val id = (childCount + 1).toInt()
                    routine_id = id // 루틴 고유번호 정해주기 -> 다음 액티비티에서 사용

                    dbRef1.child(id.toString()).setValue(data)
                        .addOnSuccessListener {
                            Log.e("루틴 내용 저장", "DB에 저장 성공")
                        }.addOnFailureListener {
                            Log.e("루틴 내용 저장", "DB에 저장 실패")
                        }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("루틴 내용 저장", "Database error: $error")
                }
            })
//            dbRef1.setValue("test") // 추후 user_id로 전환
//
//            dbRef2 = FirebaseDatabase.getInstance().getReference("UsersRoutine").child("test")
//            dbRef2.setValue(data)

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetWalkStepActivity::class.java))
        }
    }

    private fun pushTime() {
        picker = timePicker
        // picker.is24HourView = true

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