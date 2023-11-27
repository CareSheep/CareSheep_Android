package com.swu.caresheep.guardian.report

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.start.user_id
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.progress_bar_1
import kotlinx.android.synthetic.main.activity_guardian_elder_walk_report.walk_check1
import kotlinx.android.synthetic.main.activity_guardian_elder_walk_report.walk_check2
import kotlinx.android.synthetic.main.activity_guardian_elder_walk_report.walk_check3
import kotlinx.android.synthetic.main.activity_guardian_elder_walk_report.walk_check4
import kotlinx.android.synthetic.main.activity_guardian_elder_walk_report.walk_check5
import kotlinx.android.synthetic.main.activity_guardian_elder_walk_report.walk_check6
import kotlinx.android.synthetic.main.activity_guardian_elder_walk_report.walk_check7
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.swu.caresheep.databinding.ActivityGuardianElderWalkReportBinding

class GuardianElderWalkReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianElderWalkReportBinding

    // 날짜 포맷
    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    // 오늘의 날짜
    val today = LocalDate.now()

    lateinit var thisweekText : TextView

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

    private fun getThisWeek(){

        val formattedThisSunday = thisMonday.format(dateFormat)


        thisweekText.setText("$monday ~ $sunday")

        println("Today: ${today.format(dateFormat)}")
        println("This Sunday: $formattedThisSunday")
        Log.d("Sunday",formattedThisSunday)
    }

    private fun getWalkThisWeek(){
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
                                // 월요일
                                if (dateValue == "$monday") {
                                    val walk1_value = data.child("done").getValue(Int::class.java)
                                    if (walk1_value == 1) {
                                        Log.d("test_success","$walk1_value")
                                        walk_check1.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val walk2_value = data.child("done").getValue(Int::class.java)
                                    if (walk2_value == 1) {
                                        Log.d("test_success","$walk2_value")
                                        walk_check2.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val walk3_value = data.child("done").getValue(Int::class.java)
                                    if (walk3_value == 1) {
                                        Log.d("test_success","$walk3_value")
                                        walk_check3.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val walk4_value = data.child("done").getValue(Int::class.java)
                                    if (walk4_value == 1) {
                                        Log.d("test_success","$walk4_value")
                                        walk_check4.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val walk5_value = data.child("done").getValue(Int::class.java)
                                    if (walk5_value == 1) {
                                        Log.d("test_success","$walk5_value")
                                        walk_check5.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val walk6_value = data.child("done").getValue(Int::class.java)
                                    if (walk6_value == 1) {
                                        Log.d("test_success","$walk6_value")
                                        walk_check6.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val walk7_value = data.child("done").getValue(Int::class.java)
                                    if (walk7_value == 1) {
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_elder_walk_report)

        getThisWeek()
        getWalkThisWeek()

    }
}