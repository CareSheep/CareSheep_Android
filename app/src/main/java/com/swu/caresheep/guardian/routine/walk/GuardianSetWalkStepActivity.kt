package com.swu.caresheep.guardian.routine.walk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import com.swu.caresheep.guardian.routine.meal.GuardianSetBreakfastTimeActivity
import kotlinx.android.synthetic.main.activity_guardian_set_walk_step.setWalkStepButton
import kotlinx.android.synthetic.main.activity_guardian_set_walk_step.walkStepText

class GuardianSetWalkStepActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    var step : Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_set_walk_step)



//        val data = hashMapOf(
//            "walk_step" to step,
//        )

        setWalkStepButton.setOnClickListener {

            // 정수형으로 변환
            val stepText = walkStepText.text.toString()
            val step = stepText.toInt()

            val updatedData = HashMap<String, Any>()
            updatedData["walk_step"] = step

            dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("UsersRoutine").child("$routine_id")
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