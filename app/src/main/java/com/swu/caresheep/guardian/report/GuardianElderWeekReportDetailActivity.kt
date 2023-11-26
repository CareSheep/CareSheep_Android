package com.swu.caresheep.guardian.report

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianElderWeekReportDetailBinding
import com.swu.caresheep.utils.dialog.InputDialog

class GuardianElderWeekReportDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianElderWeekReportDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianElderWeekReportDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

}