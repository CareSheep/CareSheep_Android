package com.swu.caresheep.guardian.report

import android.content.Intent
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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.start.user_id
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.breakcount
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.breakper
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.dinnercount
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.dinnerper
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.lunchcount
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.lunchper
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.progressbar_fri
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.progressbar_mon
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.progressbar_sat
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.progressbar_sun
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.progressbar_thur
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.progressbar_tue
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.progressbar_wed
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.walkcount
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.walkper
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.swu.caresheep.databinding.FragmentGuardianElderWeekReport2Binding
import com.swu.caresheep.guardian.mypage.GuardianElderRoutineActivity
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.breakbt
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.dinnerbt
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.lunchbt
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.this_week
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report2.walkbt

class GuardianElderWeekReport2Fragment : Fragment() {

    //private lateinit var binding: FragmentGuardianElderWeekReport2Binding

    // 이번주 평균 루틴 이행율
    var average: Double = 0.0

    // 루틴 이행률%
    var permon: Int = 0
    var pertue: Int = 0
    var perwed: Int = 0
    var perthur: Int = 0
    var perfri: Int = 0
    var persat: Int = 0
    var persun: Int = 0

    // 달성수
    var bcount : Int =0
    var lcount : Int =0;
    var dcount : Int =0;
    var wcount : Int =0;

    // 이행율
    lateinit var percentageTextLunch : TextView
    lateinit var percentageTextBreakfast : TextView
    lateinit var percentageTextDinner : TextView
    lateinit var percentageTextWalk : TextView

    var percentage: Double = 0.0
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // binding = FragmentGuardianElderWeekReport2Binding.inflate(inflater, container, false)

        // 버튼 클릭시 이동
//        breakbt.setOnClickListener{
//            val intent = Intent(requireContext(), GuardianElderBreakfastReportActivity::class.java)
//            startActivity(intent)
//        }
//
//        lunchbt.setOnClickListener{
//            val intent = Intent(requireContext(), GuardianElderLunchReportActivity::class.java)
//            startActivity(intent)
//        }
//
//        dinnerbt.setOnClickListener{
//            val intent = Intent(requireContext(), GuardianElderDinnerReportActivity::class.java)
//            startActivity(intent)
//        }
//
//        walkbt.setOnClickListener{
//            val intent = Intent(requireContext(), GuardianElderWalkReportActivity::class.java)
//            startActivity(intent)
//        }


        getThisWeek()
        getBreakfastThisWeek()
        getLunchThisWeek()
        getDinnerThisWeek()
        getWalkThisWeek()

        breakper.setText((bcount / 28.0 * 100.0).toString())
        breakper.setText(DecimalFormat("##00.0").format((bcount / 28.0 * 100.0)).toString())

        lunchper.setText((lcount / 28.0 * 100.0).toString())
        lunchper.setText(DecimalFormat("##00.0").format((lcount / 28.0 * 100.0)).toString())

        dinnerper.setText((dcount / 28.0 * 100.0).toString())
        dinnerper.setText(DecimalFormat("##00.0").format((dcount / 28.0 * 100.0)).toString())

        walkper.setText((wcount / 28.0 * 100.0).toString())
        walkper.setText(DecimalFormat("##00.0").format((wcount / 28.0 * 100.0)).toString())

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guardian_elder_week_report2, container, false)
    }

    private fun getThisWeek(){

        val formattedThisSunday = thisMonday.format(dateFormat)


        thisweekText.setText("$monday ~ $sunday")
        this_week.setText("$monday ~ $sunday")

        println("Today: ${today.format(dateFormat)}")
        println("This Sunday: $formattedThisSunday")
        Log.d("Sunday",formattedThisSunday)

    }

    private fun getBreakfastThisWeek(){
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
                                // 월요일
                                if (dateValue == "$monday") {
                                    val breakfast1_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast1_value == 1) {
                                        bcount++
                                        breakcount.setText(bcount.toString())
                                        breakper.setText(DecimalFormat("##00.0").format((bcount / 28.0 * 100.0)).toString())
                                        permon++
                                        progressbar_mon.setProgress((permon*25).toInt())
                                        Log.d("test_success","$breakfast1_value")
                                        Log.d("test_success_per","$permon")
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val breakfast2_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast2_value == 1) {
                                        bcount++
                                        breakcount.setText(bcount.toString())
                                        breakper.setText(DecimalFormat("##00.0").format((bcount / 28.0 * 100.0)).toString())
                                        pertue++
                                        progressbar_tue.setProgress((pertue*25).toInt())
                                        Log.d("test_success","$breakfast2_value")
                                        Log.d("test_success_per2","$pertue")
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val breakfast3_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast3_value == 1) {
                                        bcount++
                                        breakcount.setText(bcount.toString())
                                        breakper.setText(DecimalFormat("##00.0").format((bcount / 28.0 * 100.0)).toString())
                                        perwed++
                                        progressbar_wed.setProgress((perwed*25).toInt())
                                        Log.d("test_success","$breakfast3_value")
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val breakfast4_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast4_value == 1) {
                                        bcount++
                                        breakcount.setText(bcount.toString())
                                        breakper.setText(DecimalFormat("##00.0").format((bcount / 28.0 * 100.0)).toString())
                                        perthur++
                                        progressbar_thur.setProgress((perthur*25).toInt())
                                        Log.d("test_success","$breakfast4_value")
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val breakfast5_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast5_value == 1) {
                                        bcount++
                                        breakcount.setText(bcount.toString())
                                        breakper.setText(DecimalFormat("##00.0").format((bcount / 28.0 * 100.0)).toString())
                                        perfri++
                                        progressbar_fri.setProgress((perfri*25).toInt())
                                        Log.d("test_success","$breakfast5_value")
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val breakfast6_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast6_value == 1) {
                                        bcount++
                                        breakcount.setText(bcount.toString())
                                        breakper.setText(DecimalFormat("##00.0").format((bcount / 28.0 * 100.0)).toString())
                                        persat++
                                        progressbar_sat.setProgress((persat*25).toInt())
                                        Log.d("test_success","$breakfast6_value")
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val breakfast7_value = data.child("done").getValue(Int::class.java)
                                    if (breakfast7_value == 1) {
                                        bcount++
                                        breakcount.setText(bcount.toString())
                                        breakper.setText(DecimalFormat("##00.0").format((bcount / 28.0 * 100.0)).toString())
                                        persun++
                                        progressbar_sun.setProgress((persun*25).toInt())
                                        Log.d("test_success","$breakfast7_value")
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
                                        lcount++
                                        lunchcount.setText(lcount.toString())
                                        lunchper.setText(DecimalFormat("##00.0").format((lcount / 28.0 * 100.0)).toString())
                                        permon++
                                        progressbar_mon.setProgress((permon*25).toInt())
                                        Log.d("test_success","$lunch1_value")
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val lunch2_value = data.child("done").getValue(Int::class.java)
                                    if (lunch2_value == 1) {
                                        lcount++
                                        lunchcount.setText(lcount.toString())
                                        lunchper.setText(DecimalFormat("##00.0").format((lcount / 28.0 * 100.0)).toString())
                                        pertue++
                                        progressbar_tue.setProgress((pertue*25).toInt())
                                        Log.d("test_success","$lunch2_value")
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val lunch3_value = data.child("done").getValue(Int::class.java)
                                    if (lunch3_value == 1) {
                                        lcount++
                                        lunchcount.setText(lcount.toString())
                                        lunchper.setText(DecimalFormat("##00.0").format((lcount / 28.0 * 100.0)).toString())
                                        perwed++
                                        progressbar_wed.setProgress((perwed*25).toInt())
                                        Log.d("test_success","$lunch3_value")
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val lunch4_value = data.child("done").getValue(Int::class.java)
                                    if (lunch4_value == 1) {
                                        lcount++
                                        lunchcount.setText(lcount.toString())
                                        lunchper.setText(DecimalFormat("##00.0").format((lcount / 28.0 * 100.0)).toString())
                                        perthur++
                                        progressbar_thur.setProgress((perthur*25).toInt())
                                        Log.d("test_success","$lunch4_value")
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val lunch5_value = data.child("done").getValue(Int::class.java)
                                    if (lunch5_value == 1) {
                                        lcount++
                                        lunchcount.setText(lcount.toString())
                                        lunchper.setText(DecimalFormat("##00.0").format((lcount / 28.0 * 100.0)).toString())
                                        perfri++
                                        progressbar_fri.setProgress((perfri*25).toInt())
                                        Log.d("test_success","$lunch5_value")
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val lunch6_value = data.child("done").getValue(Int::class.java)
                                    if (lunch6_value == 1) {
                                        lcount++
                                        lunchcount.setText(lcount.toString())
                                        lunchper.setText(DecimalFormat("##00.0").format((lcount / 28.0 * 100.0)).toString())
                                        persat++
                                        progressbar_sat.setProgress((persat*25).toInt())
                                        Log.d("test_success","$lunch6_value")
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val lunch7_value = data.child("done").getValue(Int::class.java)
                                    if (lunch7_value == 1) {
                                        lcount++
                                        lunchcount.setText(lcount.toString())
                                        lunchper.setText(DecimalFormat("##00.0").format((lcount / 28.0 * 100.0)).toString())
                                        persun++
                                        progressbar_sun.setProgress((persun*25).toInt())
                                        Log.d("test_success","$lunch7_value")
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
                                        dcount++
                                        dinnercount.setText(dcount.toString())
                                        dinnerper.setText(DecimalFormat("##00.0").format((dcount / 28.0 * 100.0)).toString())
                                        permon++
                                        progressbar_mon.setProgress((permon*25).toInt())
                                        Log.d("test_success","$dinner1_value")
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val dinner2_value = data.child("done").getValue(Int::class.java)
                                    if (dinner2_value == 1) {
                                        dcount++
                                        dinnercount.setText(dcount.toString())
                                        dinnerper.setText(DecimalFormat("##00.0").format((dcount / 28.0 * 100.0)).toString())
                                        pertue++
                                        progressbar_tue.setProgress((pertue*25).toInt())
                                        Log.d("test_success","$dinner2_value")
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val dinner3_value = data.child("done").getValue(Int::class.java)
                                    if (dinner3_value == 1) {
                                        perwed++
                                        progressbar_wed.setProgress((perwed*25).toInt())
                                        Log.d("test_success","$dinner3_value")
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val dinner4_value = data.child("done").getValue(Int::class.java)
                                    if (dinner4_value == 1) {
                                        dcount++
                                        dinnercount.setText(dcount.toString())
                                        dinnerper.setText(DecimalFormat("##00.0").format((dcount / 28.0 * 100.0)).toString())
                                        perthur++
                                        progressbar_thur.setProgress((perthur*25).toInt())
                                        Log.d("test_success","$dinner4_value")
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val dinner5_value = data.child("done").getValue(Int::class.java)
                                    if (dinner5_value == 1) {
                                        dcount++
                                        dinnercount.setText(dcount.toString())
                                        dinnerper.setText(DecimalFormat("##00.0").format((dcount / 28.0 * 100.0)).toString())
                                        perfri++
                                        progressbar_fri.setProgress((perfri*25).toInt())
                                        Log.d("test_success","$dinner5_value")
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val dinner6_value = data.child("done").getValue(Int::class.java)
                                    if (dinner6_value == 1) {
                                        dcount++
                                        dinnercount.setText(dcount.toString())
                                        dinnerper.setText(DecimalFormat("##00.0").format((dcount / 28.0 * 100.0)).toString())
                                        persat++
                                        progressbar_sat.setProgress((persat*25).toInt())
                                        Log.d("test_success","$dinner6_value")
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val dinner7_value = data.child("done").getValue(Int::class.java)
                                    if (dinner7_value == 1) {
                                        dcount++
                                        dinnercount.setText(dcount.toString())
                                        dinnerper.setText(DecimalFormat("##00.0").format((dcount / 28.0 * 100.0)).toString())
                                        persun++
                                        progressbar_sun.setProgress((persun*25).toInt())
                                        Log.d("test_success","$dinner7_value")
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
                                        wcount++
                                        walkcount.setText(wcount.toString())
                                        walkper.setText(DecimalFormat("##00.0").format((wcount / 28.0 * 100.0)).toString())
                                        permon++
                                        progressbar_mon.setProgress((permon*25).toInt())
                                        Log.d("test_success","$walk1_value")
                                    }
                                }
                                // 화요일
                                if (dateValue == "$tuesday") {
                                    val walk2_value = data.child("done").getValue(Int::class.java)
                                    if (walk2_value == 1) {
                                        wcount++
                                        walkcount.setText(wcount.toString())
                                        walkper.setText(DecimalFormat("##00.0").format((wcount / 28.0 * 100.0)).toString())
                                        pertue++
                                        progressbar_tue.setProgress((pertue*25).toInt())
                                        Log.d("test_success","$walk2_value")
                                    }
                                }
                                // 수요일
                                if (dateValue == "$wednesday") {
                                    val walk3_value = data.child("done").getValue(Int::class.java)
                                    if (walk3_value == 1) {
                                        wcount++
                                        walkcount.setText(wcount.toString())
                                        walkper.setText(DecimalFormat("##00.0").format((wcount / 28.0 * 100.0)).toString())
                                        perwed++
                                        progressbar_wed.setProgress((perwed*25).toInt())
                                        Log.d("test_success","$walk3_value")
                                    }
                                }
                                // 목요일
                                if (dateValue == "$thursday") {
                                    val walk4_value = data.child("done").getValue(Int::class.java)
                                    if (walk4_value == 1) {
                                        wcount++
                                        walkcount.setText(wcount.toString())
                                        walkper.setText(DecimalFormat("##00.0").format((wcount / 28.0 * 100.0)).toString())
                                        perthur++
                                        progressbar_thur.setProgress((perthur*25).toInt())
                                        Log.d("test_success","$walk4_value")
                                    }
                                }
                                // 금요일
                                if (dateValue == "$friday") {
                                    val walk5_value = data.child("done").getValue(Int::class.java)
                                    if (walk5_value == 1) {
                                        wcount++
                                        walkcount.setText(wcount.toString())
                                        walkper.setText(DecimalFormat("##00.0").format((wcount / 28.0 * 100.0)).toString())
                                        perfri++
                                        progressbar_fri.setProgress((perfri*25).toInt())
                                        Log.d("test_success","$walk5_value")
                                    }
                                }
                                // 토요일
                                if (dateValue == "$saturday") {
                                    val walk6_value = data.child("done").getValue(Int::class.java)
                                    if (walk6_value == 1) {
                                        wcount++
                                        walkcount.setText(wcount.toString())
                                        walkper.setText(DecimalFormat("##00.0").format((wcount / 28.0 * 100.0)).toString())
                                        persat++
                                        progressbar_sat.setProgress((persat*25).toInt())
                                        Log.d("test_success","$walk6_value")
                                    }
                                }
                                // 일요일
                                if (dateValue == "$sunday") {
                                    val walk7_value = data.child("done").getValue(Int::class.java)
                                    if (walk7_value == 1) {
                                        wcount++
                                        walkcount.setText(wcount.toString())
                                        walkper.setText(DecimalFormat("##00.0").format((wcount / 28.0 * 100.0)).toString())
                                        persun++
                                        progressbar_sun.setProgress((persun*25).toInt())
                                        Log.d("test_success","$walk7_value")
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