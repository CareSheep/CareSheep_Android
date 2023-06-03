package com.swu.caresheep.ui.guardian.medicine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.blackMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.blueMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.brownMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.greenMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.purpleMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.redMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.yellowMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.medicine_next1_button

var medicine_id : Int = 0

class GuardianSetMedicineColorActivity : AppCompatActivity() {

    private lateinit var dbRef1: DatabaseReference
    private lateinit var dbRef2: DatabaseReference
    var color : String = "" // 약의 색상

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_medicine_color)

        redMedicine.setOnClickListener {
            color = "red"
        }
        yellowMedicine.setOnClickListener {
            color = "yellow"
        }
        greenMedicine.setOnClickListener {
            color = "green"
        }
        blueMedicine.setOnClickListener {
            color = "blue"
        }
        purpleMedicine.setOnClickListener {
            color = "purple"
        }
        brownMedicine.setOnClickListener {
            color = "brown"
        }
        blackMedicine.setOnClickListener {
            color = "black"
        }

        medicine_next1_button.setOnClickListener {
            val data = hashMapOf(
                "color" to color,
                "count" to 0,
                "medicine_name" to "",
                "single_dose" to 0,
                "time" to "",
                "user_id" to 1,
            )

            dbRef1 = FirebaseDatabase.getInstance(DB_URL).getReference("MedicineTime")
            dbRef1.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val childCount = dataSnapshot.childrenCount
                    val id = (childCount + 1).toInt()
                    medicine_id = id // 약 고유번호 정해주기 -> 다음 액티비티에서 사용

                    dbRef1.child(id.toString()).setValue(data)
                        .addOnSuccessListener {
                            Log.e("복약내용 색상", "DB에 저장 성공")
                        }.addOnFailureListener {
                            Log.e("복약내용 색상", "DB에 저장 실패")
                        }
                    }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("복약내용- 색상", "Database error: $error")
                }
            })
//            dbRef1.setValue("2")
//
//            dbRef2 = FirebaseDatabase.getInstance().getReference("TakingMedicine").child("2")
//            dbRef2.setValue(data)

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetMedicineNameActivity::class.java))
        }
    }
}