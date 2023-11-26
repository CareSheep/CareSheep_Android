package com.swu.caresheep.guardian.report

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianElderWeekReportDetailBinding
import com.swu.caresheep.start.user_id
import com.swu.caresheep.utils.dialog.InputDialog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GuardianElderWeekReportDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianElderWeekReportDetailBinding

    private val today = LocalDate.now()
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val thisMonday = today.with(DayOfWeek.MONDAY)

    private val daysOfWeekFormatted = (0..6).map { thisMonday.plusDays(it.toLong()).format(dateFormat) }

    private val monday = daysOfWeekFormatted[0]
    private val tuesday = daysOfWeekFormatted[1]
    private val wednesday = daysOfWeekFormatted[2]
    private val thursday = daysOfWeekFormatted[3]
    private val friday = daysOfWeekFormatted[4]
    private val saturday = daysOfWeekFormatted[5]
    private val sunday = daysOfWeekFormatted[6]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianElderWeekReportDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 주간 날짜 업데이트
        "$monday ~ $sunday".also { binding.tvWeekDate.text = it }

        // 아침, 점심, 저녁, 운동 주간 루틴 수행 여부 업데이트
        updateBreakfastThisWeek("Breakfast")

        // 오늘의 식단 추천
        binding.btnRoutineRecommend.setOnClickListener {
            // 어르신 질병명 입력 다이얼로그 표시
            getDiseaseNameDialog()
        }

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
    }

    /**
     * 어르신 질병명 입력 Dialog 표시
     */
    private fun getDiseaseNameDialog() {
        val inputDialog = InputDialog(this)
        inputDialog.show(
            getString(R.string.guardian_report_meal_recommend_title),
            getString(R.string.guardian_report_meal_recommend_caption),
            getString(R.string.guardian_report_meal_recommend_continue),
            getString(R.string.cancel)
        )

        inputDialog.topBtnClickListener {
            // 식단 추천 화면으로 이동
            startActivity(Intent(this, GuardianElderMealRecommendActivity::class.java))
        }

        inputDialog.bottomBtnClickListener {
        }
    }

    /**
     * 어르신 주간 루틴 수행 여부 업데이트
     */
    private fun updateBreakfastThisWeek(routineName: String) {
        val dayTextViewMap = mapOf(
            monday to binding.tvRoutineCheckMon,
            tuesday to binding.tvRoutineCheckTue,
            wednesday to binding.tvRoutineCheckWed,
            thursday to binding.tvRoutineCheckThu,
            friday to binding.tvRoutineCheckFri,
            saturday to binding.tvRoutineCheckSat,
            sunday to binding.tvRoutineCheckSun,
        )

        Firebase.database(BuildConfig.DB_URL)
            .getReference(routineName)
            .orderByChild("user_id")
            .equalTo(user_id.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val dateValue = data.child("date").getValue(String::class.java)
                            val dayTextView = dayTextViewMap[dateValue]

                            if (dayTextView != null) {
                                val routineDoneValue = data.child("done").getValue(Int::class.java)
                                if (routineDoneValue == 1) {
                                    val drawable = ResourcesCompat.getDrawable(resources, R.drawable.baseline_check_circle_24, theme)
                                    dayTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 쿼리 실행 중 오류 발생 시 처리할 내용
                }
            })
    }

}