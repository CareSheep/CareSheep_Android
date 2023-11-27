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
import kotlinx.android.synthetic.main.activity_guardian_elder_lunch_report.lunch_check1
import kotlinx.android.synthetic.main.activity_guardian_elder_lunch_report.lunch_check2
import kotlinx.android.synthetic.main.activity_guardian_elder_lunch_report.lunch_check3
import kotlinx.android.synthetic.main.activity_guardian_elder_lunch_report.lunch_check4
import kotlinx.android.synthetic.main.activity_guardian_elder_lunch_report.lunch_check5
import kotlinx.android.synthetic.main.activity_guardian_elder_lunch_report.lunch_check6
import kotlinx.android.synthetic.main.activity_guardian_elder_lunch_report.lunch_check7
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GuardianElderLunchReportActivity : AppCompatActivity() {
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

    private fun getLunchThisWeek(){
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
                                // 월요일
                                if (dateValue == "$monday") {
                                    val lunch1_value = data.child("done").getValue(Int::class.java)
                                    if (lunch1_value == 1) {
                                        Log.d("test_success","$lunch1_value")
                                        lunch_check1.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val lunch2_value = data.child("done").getValue(Int::class.java)
                                    if (lunch2_value == 1) {

                                        Log.d("test_success","$lunch2_value")
                                        lunch_check2.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val lunch3_value = data.child("done").getValue(Int::class.java)
                                    if (lunch3_value == 1) {
                                        Log.d("test_success","$lunch3_value")
                                        lunch_check3.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val lunch4_value = data.child("done").getValue(Int::class.java)
                                    if (lunch4_value == 1) {
                                        Log.d("test_success","$lunch4_value")
                                        lunch_check4.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val lunch5_value = data.child("done").getValue(Int::class.java)
                                    if (lunch5_value == 1) {
                                        Log.d("test_success","$lunch5_value")
                                        lunch_check5.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val lunch6_value = data.child("done").getValue(Int::class.java)
                                    if (lunch6_value == 1) {
                                        Log.d("test_success","$lunch6_value")
                                        lunch_check6.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val lunch7_value = data.child("done").getValue(Int::class.java)
                                    if (lunch7_value == 1) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_elder_lunch_report)

        getThisWeek()
        getLunchThisWeek()
    }
}