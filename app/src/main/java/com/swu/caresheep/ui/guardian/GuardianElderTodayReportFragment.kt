package com.swu.caresheep.ui.guardian

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.ui.start.user_id
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.breakfast_check
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.dinner_check
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.lunch_check
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.view.today_date
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.walk_check
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.walk_step_today
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check1
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check2
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check3
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check4
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check5
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check6
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check7
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
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Breakfast")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("date").getValue(String::class.java)
                                // 월요일
                                if (dateValue == "$todayDate") {
                                    val breakfast1_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast1_value == 1) {
                                        Log.d("test_success","$breakfast1_value")
                                        breakfast_check.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }

    }

    private fun getTodayLunchData() {
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Lunch")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("date").getValue(String::class.java)
                                // 월요일
                                if (dateValue == "$todayDate") {
                                    val breakfast1_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast1_value == 1) {
                                        Log.d("test_success","$breakfast1_value")
                                        lunch_check.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }

    }

    private fun getTodayDinnerData() {
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Dinner")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("date").getValue(String::class.java)
                                // 월요일
                                if (dateValue == "$todayDate") {
                                    val breakfast1_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast1_value == 1) {
                                        Log.d("test_success","$breakfast1_value")
                                        dinner_check.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun getTodayWalkData() {

        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Walk")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("start_time").getValue(String::class.java)
                                // 월요일
                                if (dateValue == "$todayDate") {
                                    val breakfast1_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast1_value == 1) {
                                        Log.d("test_success","$breakfast1_value")
                                        walk_check.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                    val walk_step = data.child("walk").getValue(Int::class.java)
                                    walk_step_today.setText("$walk_step")
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }



//        dbRef = FirebaseDatabase.getInstance().getReference("Walk").child("test")
//
//        dbRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()){
//                    if(snapshot.child("start_time").getValue().toString() == "$todayDate"){
//                        val walk_value = snapshot.child("done").getValue().toString()
//                        if(walk_value == "1"){
//                            println("this is Lunch result: $walk_value")
//                            walk_check.setImageResource(R.drawable.baseline_check_circle_24)
//                        }
//                        val walk_step = snapshot.child("walk").getValue().toString()
//                        walk_step_today.setText("$walk_step")
//
//                    }
//
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                println("Failed to read value.")
//            }
//        })
    }

}