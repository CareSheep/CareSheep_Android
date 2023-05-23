package com.swu.caresheep.ui.guardian.medicine

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.R
import com.swu.caresheep.ui.guardian.GuardianActivity
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_time4.fourthMedcineButton
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_time4.fourththirdMedcinetimePicker
import java.util.HashMap

var result4 : String = ""

class GuardianSetMedicineTimeActivity4 : AppCompatActivity() {

    // 데이터 삽입
    private lateinit var dbRef7 : DatabaseReference

    // 약 복용횟수 비교용
    private lateinit var dbRef8 : DatabaseReference

    private lateinit var picker: TimePicker

    var timehour: Int = 0
    var timeminute: Int = 0
    var am_pm: String = "am"

    var countMatched : Int = 0
    var nextpage : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_medicine_time4)

        fourthMedcineButton.setOnClickListener {
            Log.d("T", "$countMedicine")
            pushTime()

            dbRef7 = FirebaseDatabase.getInstance().getReference("TakingMedicine").child("$medicine_id")
            dbRef8 = FirebaseDatabase.getInstance().getReference("TakingMedicine").child("$medicine_id")

            val updatedData = HashMap<String, Any>()
            updatedData["time"] = "$result1,$result2,$result3,$result4"

            dbRef7.updateChildren(updatedData).addOnSuccessListener(){
                println("Data updated successfully")
            }.addOnFailureListener {
                println("Error updating data: $it")
            }

            // 데이터 읽어와서 비교하기
            dbRef8.addValueEventListener(object : ValueEventListener {
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
        fourththirdMedcinetimePicker.is24HourView()
        picker = fourththirdMedcinetimePicker

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

        result4 = "$timehour:$timeminute"
        // print(result)
        Log.d("T",result4)

    }
}