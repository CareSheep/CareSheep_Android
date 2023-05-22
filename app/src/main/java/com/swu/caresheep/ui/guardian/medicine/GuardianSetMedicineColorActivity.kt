package com.swu.caresheep.ui.guardian.medicine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.blackMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.blueMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.brownMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.greenMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.purpleMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.redMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.yellowMedicine
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_color.medicine_next1_button

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

            dbRef1 = FirebaseDatabase.getInstance().getReference("TakingMedicine")
            dbRef1.setValue("2")

            dbRef2 = FirebaseDatabase.getInstance().getReference("TakingMedicine").child("2")
            dbRef2.setValue(data)

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetMedicineNameActivity::class.java))

        }
    }
}