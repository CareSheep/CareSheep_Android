package com.swu.caresheep.guardian.report

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
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

    private val daysOfWeekFormatted =
        (0..6).map { thisMonday.plusDays(it.toLong()).format(dateFormat) }

    private val monday = daysOfWeekFormatted[0]
    private val tuesday = daysOfWeekFormatted[1]
    private val wednesday = daysOfWeekFormatted[2]
    private val thursday = daysOfWeekFormatted[3]
    private val friday = daysOfWeekFormatted[4]
    private val saturday = daysOfWeekFormatted[5]
    private val sunday = daysOfWeekFormatted[6]

    private lateinit var routineName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianElderWeekReportDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            finish()
        }

        // 루틴 이름 값 받아오기
        routineName = intent.getStringExtra("routine_name").toString()
        when (routineName) {
            "Breakfast" -> {
                binding.btnRoutineRecommend.visibility = View.VISIBLE
                binding.ivRoutineRecommend.setImageResource(R.drawable.ic_report_meal)
                binding.ivRoutine.setBackgroundResource(R.drawable.breakfast_round_image)
                binding.ivRoutine.setImageResource(R.drawable.baseline_rice_bowl_24)
                binding.tvRoutineRateTitle.text = "주간 아침 식사 루틴 이행률"
                binding.tvRoutineTitle.text = "아침 식사"
                binding.btnRoutineRecommend.text = "오늘의 아침 식단 추천"
                binding.tvRoutineRecommend.text = "어르신 아침 식사 루틴 추천"
                binding.tvRoutineRecommendSubtitle.text = "건강한 아침 식사"
                binding.tvRoutineRecommendCaption.text = "어르신의 아침 식사 습관은 인지 능력에 도움이 됩니다."
                binding.tvRoutineRecommendFirst.text = "가벼운 아침 식사 식단 준비하기"
                binding.tvRoutineRecommendSecond.text = "아침에 스트레칭 후 식사하기"
                val drawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_circle_thumb_up_yellow,
                    theme
                )
                binding.tvRoutineRecommendFirst.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
                binding.tvRoutineRecommendSecond.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
            }
            "Lunch" -> {
                binding.btnRoutineRecommend.visibility = View.VISIBLE
                binding.ivRoutineRecommend.setImageResource(R.drawable.ic_report_meal)
                binding.ivRoutine.setBackgroundResource(R.drawable.lunch_round_image)
                binding.ivRoutine.setImageResource(R.drawable.baseline_rice_bowl_24)
                binding.tvRoutineRateTitle.text = "주간 점심 식사 루틴 이행률"
                binding.tvRoutineTitle.text = "점심 식사"
                binding.btnRoutineRecommend.text = "오늘의 점심 식단 추천"
                binding.tvRoutineRecommend.text = "어르신 점심 식사 루틴 추천"
                binding.tvRoutineRecommendSubtitle.text = "건강한 점심 식사"
                binding.tvRoutineRecommendCaption.text = "어르신의 점심 식사 습관은 인지 능력에 도움이 됩니다."
                binding.tvRoutineRecommendFirst.text = "단백질 위주 점심 식사 식단 준비하기"
                binding.tvRoutineRecommendSecond.text = "식사 후 걷기 운동하기"
                val drawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_circle_thumb_up_yellow,
                    theme
                )
                binding.tvRoutineRecommendFirst.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
                binding.tvRoutineRecommendSecond.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
            }
            "Dinner" -> {
                binding.btnRoutineRecommend.visibility = View.VISIBLE
                binding.ivRoutineRecommend.setImageResource(R.drawable.ic_report_meal)
                binding.ivRoutine.setBackgroundResource(R.drawable.dinner_round_image)
                binding.ivRoutine.setImageResource(R.drawable.baseline_rice_bowl_24)
                binding.tvRoutineRateTitle.text = "주간 저녁 식사 루틴 이행률"
                binding.tvRoutineTitle.text = "저녁 식사"
                binding.btnRoutineRecommend.text = "오늘의 저녁 식단 추천"
                binding.tvRoutineRecommend.text = "어르신 저녁 식사 루틴 추천"
                binding.tvRoutineRecommendSubtitle.text = "건강한 저녁 식사"
                binding.tvRoutineRecommendCaption.text = "어르신의 저녁 식사 습관은 인지 능력에 도움이 됩니다."
                binding.tvRoutineRecommendFirst.text = "저염식 식사 준비하기"
                binding.tvRoutineRecommendSecond.text = "저녁 식사 후 바로 잠들지 않기"
                val drawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_circle_thumb_up_yellow,
                    theme
                )
                binding.tvRoutineRecommendFirst.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
                binding.tvRoutineRecommendSecond.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
            }
            else -> {
                binding.btnRoutineRecommend.visibility = View.GONE
                binding.ivRoutineRecommend.setImageResource(R.drawable.ic_report_exercise)
                binding.ivRoutine.setBackgroundResource(R.drawable.walk_round_image)
                binding.ivRoutine.setImageResource(R.drawable.baseline_directions_walk_24)
                binding.tvRoutineRateTitle.text = "주간 운동 루틴 이행률"
                binding.tvRoutineTitle.text = "운동"
                binding.tvRoutineRecommend.text = "어르신 운동 루틴 추천"
                binding.tvRoutineRecommendSubtitle.text = "건강한 운동"
                binding.tvRoutineRecommendCaption.text = "어르신의 꾸준한 운동 습관은 인지 능력에 도움이 됩니다."
                binding.tvRoutineRecommendFirst.text = "하루에 3000보씩 걷기"
                binding.tvRoutineRecommendSecond.text = "수영 배우기"
                val drawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_circle_thumb_up_blue,
                    theme
                )
                binding.tvRoutineRecommendFirst.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
                binding.tvRoutineRecommendSecond.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
            }
        }

        // 주간 날짜 업데이트
        "$monday ~ $sunday".also { binding.tvWeekDate.text = it }

        // 주간 루틴 수행 여부 업데이트
        updateRoutineThisWeek(routineName)

        // 오늘의 식단 추천
        binding.btnRoutineRecommend.setOnClickListener {
            // 어르신 질병명 입력 다이얼로그 표시
            getDiseaseNameDialog()
        }

    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_right, R.anim.none)
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
            val diseaseName = inputDialog.getDiseaseNameEditText().text.toString()
            if (diseaseName.isEmpty()) {
                Toast.makeText(this, "질병명을 입력하세요", Toast.LENGTH_SHORT).show()
            } else {
                // 식단 추천 화면으로 이동
                val intent = Intent(this, GuardianElderMealRecommendActivity::class.java)
                intent.putExtra("routine_name", routineName)
                intent.putExtra("disease_name", diseaseName)
                startActivity(intent)
            }
        }

        inputDialog.bottomBtnClickListener {
        }
    }

    /**
     * 어르신 주간 루틴 수행 여부 업데이트
     */
    private fun updateRoutineThisWeek(routineName: String) {
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
                            val dateValue = if (routineName == "Walk")
                                data.child("start_time").getValue(String::class.java)
                            else
                                data.child("date").getValue(String::class.java)
                            val dayTextView = dayTextViewMap[dateValue]

                            if (dayTextView != null) {
                                val routineDoneValue = data.child("done").getValue(Int::class.java)
                                if (routineDoneValue == 1) {
                                    val drawable = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.baseline_check_circle_24,
                                        theme
                                    )
                                    dayTextView.setCompoundDrawablesWithIntrinsicBounds(
                                        null,
                                        drawable,
                                        null,
                                        null
                                    )
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