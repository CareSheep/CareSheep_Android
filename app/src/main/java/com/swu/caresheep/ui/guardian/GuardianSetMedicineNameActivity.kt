package com.swu.caresheep.ui.guardian

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_name.medicine_next2_button
import kotlinx.android.synthetic.main.activity_guardian_set_medicine_name.editText

class GuardianSetMedicineNameActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    var medicine_name : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_medicine_name)

        medicine_name = editText.toString()

        medicine_next2_button.setOnClickListener {
            val data = hashMapOf(
                "medicine_name" to medicine_name,
            )

            dbRef = FirebaseDatabase.getInstance().getReference("TakingMedicine").child("2")
            dbRef.updateChildren(data as Map<String, Any>).addOnSuccessListener {
                print("User data updated successfully")
            }.addOnFailureListener {
                print( "Error updating user data: $it")
            }
            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetMedicineSingledoseActivity::class.java))

        }
    }
}