package com.swu.caresheep.guardian.report

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import com.swu.caresheep.data.api.Gpt3Api
import com.swu.caresheep.databinding.ActivityGuardianElderMealRecommendBinding

class GuardianElderMealRecommendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianElderMealRecommendBinding
    private lateinit var routineName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianElderMealRecommendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 루틴 이름 값 받아오기
        routineName = intent.getStringExtra("routine_name").toString()
        when (routineName) {
            "Breakfast" -> routineName = "아침"
            "Lunch" -> routineName = "점심"
            "Dinner" -> routineName = "저녁"
        }

        // 질병명 값 받아오기
        val diseaseName = intent.getStringExtra("disease_name").toString()

        binding.ivBack.setOnClickListener {
            finish()
        }

        // 식단 추천
        val prompt =
            "어르신의 $routineName 식사 수행률이 60% 이하이며, ${diseaseName}를 앓고 있습니다. 건강한 $routineName 식단 1가지를 추천해주고 필요한 식재료, 레시피를 알려주세요. 추천하는 이유도 마지막에 한 문장으로 설명해주세요." +
                    "대답하는 형식은 1. 추천 식단: AAA\n 2. 추천 이유: BBB\n 3. 식재료: CCC\n 4. 레시피: DDD 형식입니다. 레시피에는 번호를 매기지 마세요. 요리 이름을 한 가지 단어로 표현하세요."

        // GPT-3 API 호출 및 응답 처리
        Gpt3Api.requestRoutineRecommend(prompt) { response ->
            if (response != null) {
                // 추천된 루틴 출력
                println(response)
                val items = response.trimIndent()

                val dish = extractValue(items, "추천 식단")
                val reason = extractValue(items, "추천 이유")
                val ingredients = extractValue(items, "식재료")
                val recipe = extractValue(items, "레시피")

                runOnUiThread {
                    binding.tvRecommendedMealDetail.text = response

                    binding.tvRecommendedMeal.text = dish
                    binding.tvRecommendedMealDetail.text = reason
                    binding.tvIngredientDetail.text = ingredients
                    binding.tvRecipeDetail.text = recipe
                }

            } else {
                println("API 호출 중 오류 발생")
            }
        }

    }

    // 루틴 추천 응답에서 문자열 추출
    private fun extractValue(response: String, key: String): String {
        val regex = when (key) {
            "레시피" -> Regex("""4\. 레시피: (.+)$""")
            "추천 식단" -> Regex("""$key: (.+?)\n""")
            "추천 이유" -> Regex("""$key: (.+?)\n""")
            else -> Regex("""$key: (.+?)\n""")
        }

        val matchResult = regex.find(response)

        return matchResult?.groups?.get(1)?.value?.trim() ?: ""
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_right, R.anim.none)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
    }
}