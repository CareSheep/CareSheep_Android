package com.swu.caresheep.ui.guardian.medicine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.MedicineTime
import com.swu.caresheep.elder.AlarmReceiver
import com.swu.caresheep.ui.guardian.GuardianActivity
import com.swu.caresheep.ui.guardian.GuardianStartSetMedicineActivity
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_time.firstMedcineButton
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_time.firstMedcinetimePicker
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.walk_check
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.walk_step_today
import java.text.SimpleDateFormat
import java.util.*

var result1 : String = ""
var countMedicine: Int = 3

class GuardianSetMedicineTimeActivity : AppCompatActivity() {

    // 데이터 삽입
    private lateinit var dbRef1 : DatabaseReference

    // 약 복용횟수 비교용
    private lateinit var dbRef2 : DatabaseReference

    private lateinit var picker: TimePicker

    var timehour: Int = 0
    var timeminute: Int = 0
    var am_pm: String = "am"

    var countMatched : Int = 0
    var nextpage : Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_medicine_time)

        firstMedcineButton.setOnClickListener {

            Log.d("countMedicine", "$countMedicine")
            pushTime()
            Log.d("Time","$result1")

            dbRef1 = FirebaseDatabase.getInstance().getReference("MedicineTime").child("$medicine_id")
            dbRef2 = FirebaseDatabase.getInstance().getReference("MedicineTime").child("$medicine_id")

            val updatedData = HashMap<String, Any>()
            updatedData["time"] = "$result1"

            // 데이터 삽입
            dbRef1.updateChildren(updatedData).addOnSuccessListener(){
                println("Data updated successfully")
            }.addOnFailureListener {
                println("Error updating data: $it")
            }

            // 데이터 읽어와서 비교하기
            dbRef2.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        if(snapshot.child("count").getValue().toString() == "$countMedicine"){
                            val checkthis = snapshot.child("count").getValue().toString()
                            if(checkthis == "$countMedicine"){
                                countMatched = 1
                                nextpage = 1
                                Log.d("countMatched", "$countMatched")
                                nextPage()

                            }
                        }else{
                            nextpage = 1
                            nextPage()
                        }

                    }
                }


                override fun onCancelled(error: DatabaseError) {
                    println("Failed to read value.")
                }
            })
        }

        // Firebase Realtime Database에서 데이터 가져오기
        val database =
            FirebaseDatabase.getInstance("https://caresheep-dcb96-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val reference = database.getReference("MedicineTime")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val medicineData = childSnapshot.getValue(MedicineTime::class.java)

                    // time 필드 String으로 변환
                    val timeString = medicineData!!.time

                    Log.d("Firebase", "시간 값: $timeString")

                    // 시간 : 분 형태로 돼있어서 split으로 분리시킨 후 int로 변환
                    val timeParts = timeString.split(":")
                    if (timeParts.size == 2) {
                        val hour = timeParts[0].toIntOrNull()
                        val minute = timeParts[1].toIntOrNull()
                        // 해당 시간에 알람 설정
                        if (hour != null && minute != null) {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)

                            // 현재 시간보다 이전이면 다음 날로 설정
                            if (calendar.before(Calendar.getInstance())) {
                                calendar.add(Calendar.DATE, 1)
                            }

                            val alarmIntent = Intent(this@GuardianSetMedicineTimeActivity, AlarmReceiver::class.java)
                            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            alarmIntent.action = AlarmReceiver.ACTION_RESTART_SERVICE
                            val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                this@GuardianSetMedicineTimeActivity,
                                0,
                                alarmIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    alarmCallPendingIntent
                                )
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    alarmCallPendingIntent
                                )
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 가져오기가 실패한 경우 처리할 내용
                val exception = databaseError.toException()
                Log.e("FirebaseDatabase", "Failed to read value: ${exception.message}", exception)

            }
        })



    }

    private fun nextPage() {
        // 만약에 복약횟수랑 같으면 홈으로 이동 아니라면 다음약 설정으로 이동
        if(countMatched == 1 && nextpage == 1){
            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianActivity::class.java)) // 홈화면으로 이동(모든 루틴 설정 완료)
        }else if (nextpage == 1){
            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetMedicineTimeActivity2::class.java))
            countMedicine++ // 카운트 증가
            Log.d("증가1!","카운트 증가했고,$countMedicine")
        }
    }

    private fun pushTime() {
        picker = firstMedcinetimePicker

        val hour = picker.hour
        val minute = picker.minute

        // 시간과 분을 형식화하여 문자열로 변환
        val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }.time
        )
        result1 = timeString
        Log.d("저장되는 시간은","$result1")


        ////
//        val medicineData = MedicineTime()
//        val timeString = medicineData!!.time
//        // 시간 : 분 형태로 돼있어서 split으로 분리시킨 후 int로 변환
//        val timeParts = timeString.split(":")
//        if (timeParts.size == 2) {
//            val hour = timeParts[0].toIntOrNull()
//            val minute = timeParts[1].toIntOrNull()
//            // 해당 시간에 알람 설정
//            if (hour != null && minute != null) {
//                val calendar = Calendar.getInstance()
//                calendar.set(Calendar.HOUR_OF_DAY, hour)
//                calendar.set(Calendar.MINUTE, minute)
//                calendar.set(Calendar.SECOND, 0)
//
//                // 현재 시간보다 이전이면 다음 날로 설정
//                if (calendar.before(Calendar.getInstance())) {
//                    calendar.add(Calendar.DATE, 1)
//                }
//
//                result1 = "$hour:$minute"
//                // print(result)
//                Log.d("제발제발시간좀!!!!!!!","$result1")

                ////
        //firstMedcinetimePicker.is24HourView()


//        if (Build.VERSION.SDK_INT >= 23) {
//            timehour = picker.hour
//            timeminute = picker.minute
//        } else {
//            timehour = picker.currentHour
//            timeminute = picker.currentMinute
//        }
//
//        if (timehour > 12) {
//            am_pm = "PM"
//            timehour -= 12
//        } else {
//            am_pm = "AM"
//        }

    }
}