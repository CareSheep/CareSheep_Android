package com.swu.caresheep.guardian.alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_name.medicine_next2_button
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_name.et_medicine_name

class GuardianSetMedicineNameActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    var medicine_exist : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_medicine_name)


        medicine_next2_button.setOnClickListener {
            val medicine_name = et_medicine_name.text.toString()
            Log.d("check_name","$medicine_name")

            val data = hashMapOf(
                "medicine_name" to medicine_name,
            )

            dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("MedicineTime").child("$medicine_id")
            dbRef.updateChildren(data as Map<String, Any>).addOnSuccessListener {
                print("User data updated successfully")
                medicine_exist = 1
            }.addOnFailureListener {
                print( "Error updating user data: $it")
            }

            startActivity(Intent(this, GuardianSetMedicineSingledoseActivity::class.java))
        }
    }
}