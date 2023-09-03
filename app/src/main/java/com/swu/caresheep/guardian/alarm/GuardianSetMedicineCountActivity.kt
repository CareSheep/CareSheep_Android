package com.swu.caresheep.guardian.alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_count.count_text
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_count.medicine_next4_button
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_count.minus_button
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_count.plus_button


// 하루에 몇번 복용하는지 설정하는 액티비티
class GuardianSetMedicineCountActivity : AppCompatActivity() {

    private lateinit var dbRef : DatabaseReference
    var counter : Int = 0 // 증감할 숫자

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_medicine_count)

        plus_button.setOnClickListener {
            counter++
            count_text.text = counter.toString()
        }
        minus_button.setOnClickListener {
            counter--
            count_text.text = counter.toString()
        }

        medicine_next4_button.setOnClickListener {
            // 테스트 후 변경
            val data = hashMapOf(
                //"color" to color,
                "count" to counter,
                //"medicine_name" to medicine_name,
                //"single_dose" to 0,
                //"time" to "",
                //"user_id" to 1,
            )

            dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("MedicineTime").child("$medicine_id")
            dbRef.updateChildren(data as Map<String, Any>).addOnSuccessListener {
                print("User data updated successfully")
            }.addOnFailureListener {
                print( "Error updating user data: $it")
            }

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetMedicineTimeActivity::class.java))
        }
    }
}