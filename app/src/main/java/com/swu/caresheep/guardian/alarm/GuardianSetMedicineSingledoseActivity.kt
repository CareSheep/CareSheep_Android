package com.swu.caresheep.guardian.alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_singledose.medicine_next3_button
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_singledose.single_dose_count_text
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_singledose.single_minus_button
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_singledose.single_plus_button

// 한번 복용 당 몇개를 복용하는지 설정하는 액티비티
class GuardianSetMedicineSingledoseActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    var single_dose_counter : Int = 0 // 증감할 숫자

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_medicine_singledose)

        single_plus_button.setOnClickListener {
            single_dose_counter++
            single_dose_count_text.text = single_dose_counter.toString()
        }
        single_minus_button.setOnClickListener {
            single_dose_counter--
            single_dose_count_text.text = single_dose_counter.toString()
        }

        medicine_next3_button.setOnClickListener {
            // 테스트 후 변경
            val data = hashMapOf(
                "single_dose" to single_dose_counter,
            )

            dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("MedicineTime").child("$medicine_id")
            dbRef.updateChildren(data as Map<String, Any>).addOnSuccessListener {
                print("User data updated successfully")
            }.addOnFailureListener {
                print( "Error updating user data: $it")
            }

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetMedicineCountActivity::class.java))
        }
    }
}