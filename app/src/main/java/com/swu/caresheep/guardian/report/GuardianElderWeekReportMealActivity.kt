package com.swu.caresheep.guardian.report

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianElderWeekReportMealBinding
import com.swu.caresheep.utils.dialog.InputDialog

class GuardianElderWeekReportMealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianElderWeekReportMealBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianElderWeekReportMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 오늘의 식단 추천
        binding.btnMealRecommend.setOnClickListener {
            // 어르신 질병명 입력 다이얼로그 표시
            getDiseaseNameDialog()
        }

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
            // 식단 추천
            // 화면 이동
            finish()
        }

        inputDialog.bottomBtnClickListener {
        }
    }

}