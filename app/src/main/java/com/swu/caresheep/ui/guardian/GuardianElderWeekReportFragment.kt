package com.swu.caresheep.ui.guardian

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import com.swu.caresheep.ui.elder.main.ElderActivity
import com.swu.caresheep.ui.start.SignUpActivity
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check1
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check2
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check3
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check4
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check5
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check6
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.breakfast_check7
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.dinner_check1
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.dinner_check2
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.dinner_check3
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.dinner_check4
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.dinner_check5
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.dinner_check6
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.dinner_check7
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.lunch_check1
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.lunch_check2
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.lunch_check3
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.lunch_check4
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.lunch_check5
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.lunch_check6
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.lunch_check7
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.this_week
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.walk_check1
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.walk_check2
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.walk_check3
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.walk_check4
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.walk_check5
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.walk_check6
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.walk_check7
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter
import java.util.Calendar


class GuardianElderWeekReportFragment : Fragment() {

    // 루틴 이행률%
    var per: Int = 0
    var percentage: Double = 0.0
    // 날짜 포맷
    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    // 오늘의 날짜
    val today = LocalDate.now()

    lateinit var thisweekText : TextView
    lateinit var percentageText : TextView

    // 오늘 날짜를 기준으로 월요일~일요일을 구함, 주간 리포트의 시작은 '월요일'이 시작임
    val thisMonday = today.with(DayOfWeek.MONDAY)
   // val thisSunday = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY))
    val monday = thisMonday.format(dateFormat)
    val tuesday = thisMonday.plusDays(1).format(dateFormat)
    val wednesday = thisMonday.plusDays(2).format(dateFormat)
    val thursday = thisMonday.plusDays(3).format(dateFormat)
    val friday = thisMonday.plusDays(4).format(dateFormat)
    val saturday = thisMonday.plusDays(5).format(dateFormat)
    val sunday = thisMonday.plusDays(6).format(dateFormat)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_guardian_elder_week_report, container, false)
        val progress_percentage = view.findViewById<ProgressBar>(R.id.progress_bar)
        thisweekText = view.findViewById<TextView>(R.id.this_week)
        percentageText = view.findViewById<TextView>(R.id.percentage)

        getThisWeek()
        getBreakfastThisWeek()
        getLunchThisWeek()
        getDinnerThisWeek()
        getWalkThisWeek()
        progress_percentage.progress.compareTo(per)
        val df2 = DecimalFormat("##00.0")
        percentageText.setText(df2.format(((progress_percentage.progress.toDouble() / progress_percentage.max.toDouble()))* 100.0).toString())

//        button1.setOnClickListener {
//
//            getBreakfastThisWeek()
//            getLunchThisWeek()
//            getDinnerThisWeek()
//            getWalkThisWeek()
//            progress_percentage.progress = per
//        }

        return view

    }

    //val nextDate = dateToFind.plusDays(1)
    //val formattedNextDate = nextDate.format(dateFormat)
    private fun getThisWeek(){

        val formattedThisSunday = thisMonday.format(dateFormat)


        thisweekText.setText("$monday ~ $sunday")

        println("Today: ${today.format(dateFormat)}")
        println("This Sunday: $formattedThisSunday")
        Log.d("Sunday",formattedThisSunday)

    }

    private fun getBreakfastThisWeek(){
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(DB_URL)
                .getReference("Breakfast")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("date").getValue(String::class.java)
                                // 월요일
                                if (dateValue == "$monday") {
                                    val breakfast1_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast1_value == 1) {
                                        per++
                                        Log.d("test_success","$breakfast1_value")
                                        Log.d("test_success_per","$per")
                                        breakfast_check1.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val breakfast2_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast2_value == 1) {
                                        per++
                                        Log.d("test_success","$breakfast2_value")
                                        Log.d("test_success_per2","$per")
                                        breakfast_check2.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val breakfast3_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast3_value == 1) {
                                        per++
                                        Log.d("test_success","$breakfast3_value")
                                        breakfast_check3.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val breakfast4_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast4_value == 1) {
                                        per++
                                        Log.d("test_success","$breakfast4_value")
                                        breakfast_check4.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val breakfast5_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast5_value == 1) {
                                        per++
                                        Log.d("test_success","$breakfast5_value")
                                        breakfast_check5.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val breakfast6_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast6_value == 1) {
                                        per++
                                        Log.d("test_success","$breakfast6_value")
                                        breakfast_check6.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val breakfast7_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast7_value == 1) {
                                        per++
                                        Log.d("test_success","$breakfast7_value")
                                        breakfast_check7.setImageResource(R.drawable.baseline_check_circle_24)
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

    private fun getLunchThisWeek(){
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(DB_URL)
                .getReference("Lunch")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("date").getValue(String::class.java)
                                // 월요일
                                if (dateValue == "$monday") {
                                    val lunch1_value = data.child("done").getValue(Int::class.java)
                                    if (lunch1_value == 1) {
                                        per++
                                        Log.d("test_success","$lunch1_value")
                                        lunch_check1.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val lunch2_value = data.child("done").getValue(Int::class.java)
                                    if (lunch2_value == 1) {
                                        per++
                                        Log.d("test_success","$lunch2_value")
                                        lunch_check2.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val lunch3_value = data.child("done").getValue(Int::class.java)
                                    if (lunch3_value == 1) {
                                        per++
                                        Log.d("test_success","$lunch3_value")
                                        lunch_check3.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val lunch4_value = data.child("done").getValue(Int::class.java)
                                    if (lunch4_value == 1) {
                                        per++
                                        Log.d("test_success","$lunch4_value")
                                        lunch_check4.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val lunch5_value = data.child("done").getValue(Int::class.java)
                                    if (lunch5_value == 1) {
                                        per++
                                        Log.d("test_success","$lunch5_value")
                                        lunch_check5.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val lunch6_value = data.child("done").getValue(Int::class.java)
                                    if (lunch6_value == 1) {
                                        per++
                                        Log.d("test_success","$lunch6_value")
                                        lunch_check6.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val lunch7_value = data.child("done").getValue(Int::class.java)
                                    if (lunch7_value == 1) {
                                        per++
                                        Log.d("test_success","$lunch7_value")
                                        lunch_check7.setImageResource(R.drawable.baseline_check_circle_24)
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

    private fun getDinnerThisWeek(){
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(DB_URL)
                .getReference("Dinner")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("date").getValue(String::class.java)
                                // 월요일
                                if (dateValue == "$monday") {
                                    val dinner1_value = data.child("done").getValue(Int::class.java)
                                    if (dinner1_value == 1) {
                                        per++
                                        Log.d("test_success","$dinner1_value")
                                        dinner_check1.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val dinner2_value = data.child("done").getValue(Int::class.java)
                                    if (dinner2_value == 1) {
                                        per++
                                        Log.d("test_success","$dinner2_value")
                                        dinner_check2.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val dinner3_value = data.child("done").getValue(Int::class.java)
                                    if (dinner3_value == 1) {
                                        per++
                                        Log.d("test_success","$dinner3_value")
                                        dinner_check3.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val dinner4_value = data.child("done").getValue(Int::class.java)
                                    if (dinner4_value == 1) {
                                        per++
                                        Log.d("test_success","$dinner4_value")
                                        dinner_check4.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val dinner5_value = data.child("done").getValue(Int::class.java)
                                    if (dinner5_value == 1) {
                                        per++
                                        Log.d("test_success","$dinner5_value")
                                        dinner_check5.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val dinner6_value = data.child("done").getValue(Int::class.java)
                                    if (dinner6_value == 1) {
                                        per++
                                        Log.d("test_success","$dinner6_value")
                                        dinner_check6.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val dinner7_value = data.child("done").getValue(Int::class.java)
                                    if (dinner7_value == 1) {
                                        per++
                                        Log.d("test_success","$dinner7_value")
                                        dinner_check7.setImageResource(R.drawable.baseline_check_circle_24)
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

    private fun getWalkThisWeek(){
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(DB_URL)
                .getReference("Walk")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dateValue = data.child("start_time").getValue(String::class.java)
                                // 월요일
                                if (dateValue == "$monday") {
                                    val walk1_value = data.child("done").getValue(Int::class.java)
                                    if (walk1_value == 1) {
                                        per++
                                        Log.d("test_success","$walk1_value")
                                        walk_check1.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val walk2_value = data.child("done").getValue(Int::class.java)
                                    if (walk2_value == 1) {
                                        per++
                                        Log.d("test_success","$walk2_value")
                                        walk_check2.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val walk3_value = data.child("done").getValue(Int::class.java)
                                    if (walk3_value == 1) {
                                        per++
                                        Log.d("test_success","$walk3_value")
                                        walk_check3.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val walk4_value = data.child("done").getValue(Int::class.java)
                                    if (walk4_value == 1) {
                                        per++
                                        Log.d("test_success","$walk4_value")
                                        walk_check4.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val walk5_value = data.child("done").getValue(Int::class.java)
                                    if (walk5_value == 1) {
                                        per++
                                        Log.d("test_success","$walk5_value")
                                        walk_check5.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val walk6_value = data.child("done").getValue(Int::class.java)
                                    if (walk6_value == 1) {
                                        per++
                                        Log.d("test_success","$walk6_value")
                                        walk_check6.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val walk7_value = data.child("done").getValue(Int::class.java)
                                    if (walk7_value == 1) {
                                        per++
                                        Log.d("test_success","$walk7_value")
                                        walk_check7.setImageResource(R.drawable.baseline_check_circle_24)
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


}