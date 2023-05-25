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
import com.swu.caresheep.elder.AlarmReceiver
import com.swu.caresheep.ui.guardian.GuardianActivity
import com.swu.caresheep.ui.guardian.GuardianStartSetMedicineActivity
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_time.firstMedcineButton
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_time.firstMedcinetimePicker
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.walk_check
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.walk_step_today
import java.util.*

var result1 : String = ""
var countMedicine: Int = 1

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

            dbRef1 = FirebaseDatabase.getInstance().getReference("TakingMedicine").child("$medicine_id")
            dbRef2 = FirebaseDatabase.getInstance().getReference("TakingMedicine").child("$medicine_id")

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
        firstMedcinetimePicker.is24HourView()
        picker = firstMedcinetimePicker

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

        result1 = "$timehour:$timeminute"
        // print(result)
        Log.d("T",result1)

    }
}