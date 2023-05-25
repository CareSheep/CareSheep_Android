package com.swu.caresheep.elder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.R
import com.swu.caresheep.User
import com.swu.caresheep.ui.start.UserAgeFragment

class ElderGetAlarmTimeActivity : AppCompatActivity() {

    // 복약 시간 가져오기
    private lateinit var dbRef1: DatabaseReference

    // 아침, 점심, 저녁 시간 가져오기
    private lateinit var dbRef2: DatabaseReference

    // 아침, 점심, 저녁 시간 알람 후 데이터 베이스에 시간 저장
    private lateinit var dbRef3: DatabaseReference

    var medicine_color : String = ""
    lateinit var medicine_time : List<String>

    var breakfast_time : String = ""
    var lunch_time : String = ""
    var dinner_time : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elder_get_alarm_time)

        getUserid()

//        getMedicineColor()
//        getMedicineTime()
//        getBreakfastTime()
//        getLunchTime()
//        getDinnerTime()
    }


    private fun getUserid(){
        var gotid : Int = 0
        gotid= UserAgeFragment().id
        Log.d("user_id확인","$gotid")
//        dbRef1 = FirebaseDatabase.getInstance().getReference("TakingMedicine").child("2")
//
//        dbRef1.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()){
//                    medicine_color = snapshot.child("color").getValue().toString()
//                    Log.d("medicine_color","$medicine_color")
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                println("Failed to read value.")
//            }
//        })

    }


    /// AlarmManager 설정 시 사용하는 시간 해당 데이터들은 '시간:분' 으로 구성되어있어 각각을 시간과 분으로 나눈후
    /// alarmCalendar.set(Calendar.HOUR_OF_DAY, 2)... 등에서 사용해야 할 듯 함

    private fun getMedicineColor(){
        dbRef1 = FirebaseDatabase.getInstance().getReference("TakingMedicine").child("2")

        dbRef1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    medicine_color = snapshot.child("color").getValue().toString()
                    Log.d("medicine_color","$medicine_color")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })

    }

    private fun getMedicineTime(){

        dbRef1 = FirebaseDatabase.getInstance().getReference("TakingMedicine").child("2")

        dbRef1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    medicine_time = snapshot.child("time").getValue().toString().split(",")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })

    }

    private fun getBreakfastTime(){

        dbRef2 = FirebaseDatabase.getInstance().getReference("UsersRoutine").child("test")

        dbRef2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    breakfast_time = snapshot.child("breakfast").getValue().toString()
                    Log.d("breakfast_time","$breakfast_time")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })

    }

    private fun getLunchTime(){
        dbRef2 = FirebaseDatabase.getInstance().getReference("UsersRoutine").child("test")

        dbRef2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    lunch_time = snapshot.child("lunch").getValue().toString()
                    Log.d("lunch_time","$lunch_time")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })

    }

    private fun getDinnerTime(){
        dbRef2 = FirebaseDatabase.getInstance().getReference("UsersRoutine").child("test")

        dbRef2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    dinner_time = snapshot.child("dinner").getValue().toString()
                    Log.d("dinner_time","$dinner_time")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })

    }


    /// 알람이 울리면 실행하는 데이터 값 삽입 작업

    // 아침
    private fun insertBreakfast(){
        val data = hashMapOf(
            "date" to "",
            "done" to 0,
            "user_id" to 1
        )

        dbRef3 = FirebaseDatabase.getInstance().getReference("Breakfast")
        dbRef3.setValue("3")

        dbRef3 = FirebaseDatabase.getInstance().getReference("Breakfast").child("3")
        dbRef3.setValue(data)
    }

    // 점심
    private fun insertLunch(){
        val data = hashMapOf(
            "date" to "",
            "done" to 0,
            "user_id" to 1
        )

        dbRef3 = FirebaseDatabase.getInstance().getReference("Lunch")
        dbRef3.setValue("3")

        dbRef3 = FirebaseDatabase.getInstance().getReference("Lunch").child("3")
        dbRef3.setValue(data)
    }

    // 저녁
    private fun insertDinner(){
        val data = hashMapOf(
            "date" to "",
            "done" to 0,
            "user_id" to 1
        )

        dbRef3 = FirebaseDatabase.getInstance().getReference("Dinner")
        dbRef3.setValue("3")

        dbRef3 = FirebaseDatabase.getInstance().getReference("Dinner").child("3")
        dbRef3.setValue(data)
    }


}