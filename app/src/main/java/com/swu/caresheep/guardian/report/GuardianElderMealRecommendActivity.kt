package com.swu.caresheep.guardian.report

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.data.api.Gpt3Api
import com.swu.caresheep.databinding.ActivityGuardianElderMealRecommendBinding

class GuardianElderMealRecommendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianElderMealRecommendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianElderMealRecommendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        binding.ivBack.setOnClickListener {
            finish()
        }

        // 식단 추천
        // 사용자의 요청 문장
//        val prompt = "노인의 일주일 간 운동 수행률이 60% 이하입니다. 고령자가 운동을 자주 하지 않는 경우, 건강한 운동 습관을 유지하기 위한 두 가지 효과적인 방법을 알려주세요."
//        val prompt = "노인의 일주일 간 아침 식사 수행률이 60% 이하입니다. 고령자가 식사를 자주 거르는 경우, 건강한 식습관을 유지하기 위한 두 가지 효과적인 방법을 알려주세요."
//        val prompt = "73세 여성 어르신의 일주일 간 아침 식사 수행률이 60% 이하입니다. 어르신은 당뇨를 앓고 있습니다. 고령자를 위해 아침 식단 1가지를 추천해주고 필요한 식재료, 레시피를 알려주세요."
        val prompt =
            "73세 여성 어르신의 아침 식사 수행률이 60% 이하이며, 당뇨를 앓고 있습니다. 건강한 아침 식단 1가지를 추천해주고 필요한 식재료, 레시피를 알려주세요. 추천하는 이유도 마지막에 한 문장으로 설명해주세요." +
                    "대답하는 형식은 1. 추천 식단, 2. 레시피, 3. 추천 이유 형식입니다."

        // GPT-3 API 호출 및 응답 처리
        Gpt3Api.requestRoutineRecommend(prompt) { response ->
            if (response != null) {
                // 추천된 루틴 출력
                println(response)
            } else {
                println("API 호출 중 오류 발생")
            }
        }

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
    }
}