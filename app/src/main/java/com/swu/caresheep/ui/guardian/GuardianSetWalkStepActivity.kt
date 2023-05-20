package com.swu.caresheep.ui.guardian

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_guardian_set_walk_step.editText
import kotlinx.android.synthetic.main.activity_guardian_set_walk_step.setWalkStepButton

class GuardianSetWalkStepActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    var stepText : String = ""
    var step : Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_walk_step)

        // 정수형으로 변환
        stepText = editText.toString()
        // step = stepText.toInt()

        setWalkStepButton.setOnClickListener {

            dbRef = FirebaseDatabase.getInstance().getReference("UsersRoutine").child("test")

            val updatedData = HashMap<String, Any>()
            updatedData["walk_step"] = stepText

            dbRef.updateChildren(updatedData).addOnSuccessListener(){
                println("Data updated successfully")
            }.addOnFailureListener {
                println("Error updating data: $it")
            }

            // 다음 액티비티 이동
            startActivity(Intent(this, GuardianSetBreakfastTimeActivity::class.java))
        }
    }
}