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
import kotlinx.android.synthetic.main.activity_guardian_elder_dinner_report.dinner_check1
import kotlinx.android.synthetic.main.activity_guardian_elder_dinner_report.dinner_check2
import kotlinx.android.synthetic.main.activity_guardian_elder_dinner_report.dinner_check3
import kotlinx.android.synthetic.main.activity_guardian_elder_dinner_report.dinner_check4
import kotlinx.android.synthetic.main.activity_guardian_elder_dinner_report.dinner_check5
import kotlinx.android.synthetic.main.activity_guardian_elder_dinner_report.dinner_check6
import kotlinx.android.synthetic.main.activity_guardian_elder_dinner_report.dinner_check7
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.progress_bar_1
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GuardianElderDinnerReportActivity : AppCompatActivity() {

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

    private fun getDinnerThisWeek(){
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
                                // 월요일
                                if (dateValue == "$monday") {
                                    val dinner1_value = data.child("done").getValue(Int::class.java)
                                    if (dinner1_value == 1) {
                                        Log.d("test_success","$dinner1_value")
                                        dinner_check1.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val dinner2_value = data.child("done").getValue(Int::class.java)
                                    if (dinner2_value == 1) {
                                        Log.d("test_success","$dinner2_value")
                                        dinner_check2.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val dinner3_value = data.child("done").getValue(Int::class.java)
                                    if (dinner3_value == 1) {
                                        Log.d("test_success","$dinner3_value")
                                        dinner_check3.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val dinner4_value = data.child("done").getValue(Int::class.java)
                                    if (dinner4_value == 1) {
                                        Log.d("test_success","$dinner4_value")
                                        dinner_check4.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val dinner5_value = data.child("done").getValue(Int::class.java)
                                    if (dinner5_value == 1) {
                                        Log.d("test_success","$dinner5_value")
                                        dinner_check5.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val dinner6_value = data.child("done").getValue(Int::class.java)
                                    if (dinner6_value == 1) {
                                        Log.d("test_success","$dinner6_value")
                                        dinner_check6.setImageResource(R.drawable.baseline_check_circle_24)
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val dinner7_value = data.child("done").getValue(Int::class.java)
                                    if (dinner7_value == 1) {
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_elder_dinner_report)

        getThisWeek()
        getDinnerThisWeek()
    }
}