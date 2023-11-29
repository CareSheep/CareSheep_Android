package com.swu.caresheep.guardian.report
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.start.user_id
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.*
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.today_exercise_count
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.view.today_date
import java.time.LocalDate

class GuardianElderTodayReportFragment : Fragment() {

    // 루틴 이행률%
    var per: Int = 0

    //  식사 횟수
    var countMeal: Int = 0

    lateinit var percentageText : TextView

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
            val user_id = user_id // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Breakfast")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("date").getValue(String::class.java)
                                if (dateValue == "$todayDate") {
                                    val breakfast1_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast1_value == 1) {
                                        Log.d("test_success","$breakfast1_value")
                                        per++
                                        progress_bar_today.setProgress((per*25).toInt())
                                        countMeal++
                                        today_meal_count.setText(countMeal.toString())
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
            val user_id = user_id // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Lunch")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("date").getValue(String::class.java)
                                if (dateValue == "$todayDate") {
                                    val breakfast1_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast1_value == 1) {
                                        Log.d("test_success","$breakfast1_value")
                                        per++
                                        progress_bar_today.setProgress((per*25).toInt())
                                        countMeal++
                                        today_meal_count.setText(countMeal.toString())
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
            val user_id = user_id // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Dinner")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("date").getValue(String::class.java)
                                if (dateValue == "$todayDate") {
                                    val breakfast1_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast1_value == 1) {
                                        Log.d("test_success","$breakfast1_value")
                                        per++
                                        progress_bar_today.setProgress((per*25).toInt())
                                        countMeal++
                                        today_meal_count.setText(countMeal.toString())
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
            val user_id = user_id // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Walk")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("start_time").getValue(String::class.java)
                                if (dateValue == "$todayDate") {
                                    val breakfast1_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast1_value == 1) {
                                        Log.d("test_success","$breakfast1_value")
                                        per++
                                        progress_bar_today.setProgress((per*25).toInt())
                                        walk_check.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                    val walk_step = data.child("walk").getValue(Int::class.java)
                                    val walk_time = data.child("walk_time").getValue(String::class.java)
                                    walk_step_today.setText("$walk_step")
                                    today_exercise_count.setText(walk_time)
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

}