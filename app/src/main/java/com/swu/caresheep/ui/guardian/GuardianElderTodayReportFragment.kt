package com.swu.caresheep.ui.guardian

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.breakfast_check
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.dinner_check
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.lunch_check
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.view.today_date
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.walk_check
import java.time.LocalDate
import java.time.LocalDateTime


class GuardianElderTodayReportFragment : Fragment() {

    private lateinit var dbRef: DatabaseReference
    val todayDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        getTodayBreakfastData()
        getTodayLunchData()
        getTodayDinnerData()
        getTodayWalkData()

        val view : View = inflater!!.inflate(R.layout.fragment_guardian_elder_today_report, container, false)

        view.today_date.setText("$todayDate")
        return view
    }

    private fun getTodayBreakfastData() {

        dbRef = FirebaseDatabase.getInstance().getReference("Breakfast").child("test")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val breakfast_value = snapshot.child("done").getValue().toString()
                    if(breakfast_value == "1"){
                        println("this is Breakfast result: $breakfast_value")
                        breakfast_check.setImageResource(R.drawable.baseline_check_circle_24)
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })
    }

    private fun getTodayLunchData() {
        dbRef = FirebaseDatabase.getInstance().getReference("Lunch").child("test")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val lunch_value = snapshot.child("done").getValue().toString()

                    if(lunch_value == "1"){
                        println("this is Lunch result: $lunch_value")
                        lunch_check.setImageResource(R.drawable.baseline_check_circle_24)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })
    }

    private fun getTodayDinnerData() {
        dbRef = FirebaseDatabase.getInstance().getReference("Dinner").child("test")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val dinner_value = snapshot.child("done").getValue().toString()
                    if(dinner_value == "1"){
                        println("this is Lunch result: $dinner_value")
                        dinner_check.setImageResource(R.drawable.baseline_check_circle_24)
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })
    }

    private fun getTodayWalkData() {
        dbRef = FirebaseDatabase.getInstance().getReference("Walk").child("test")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val walk_value = snapshot.child("done").getValue().toString()
                    if(walk_value == "1"){
                        println("this is Lunch result: $walk_value")
                        walk_check.setImageResource(R.drawable.baseline_check_circle_24)
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })
    }

}