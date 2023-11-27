package com.swu.caresheep.data.api

import android.util.Log
import com.swu.caresheep.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

const val API_KEY = BuildConfig.API_KEY
const val API_URL = "https://api.openai.com/v1/completions"

object Gpt3Api {
    /**
     * 음성 사서함 - 상황별 분류
     */
    fun requestRecordClassification(
        prompt: String,
        model: String = "text-davinci-002",
        n: Int = 1,
        maxTokens: Int = 16,
        temperature: Double = 0.5,
        topP: Double = 1.0,
        frequencyPenalty: Double = 0.0,
        presencePenalty: Double = 0.0,
        callback: (String?) -> Unit
    ) {
        val jsonObject = JSONObject()
        jsonObject.put("prompt", prompt)
        jsonObject.put("model", model)
        jsonObject.put("n", n)
        jsonObject.put("max_tokens", maxTokens)
        jsonObject.put("temperature", temperature)
        jsonObject.put("top_p", topP)
        jsonObject.put("frequency_penalty", frequencyPenalty)
        jsonObject.put("presence_penalty", presencePenalty)

        val requestBody =
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.e("GPT3 RESPONSE", responseData.toString())

                val jsonObject = JSONObject(responseData)
                val choicesArray = jsonObject.getJSONArray("choices")
                if (choicesArray.length() > 0) {
                    val choice = choicesArray.getJSONObject(0)
                    val text = choice.getString("text").trim()
                    callback(text)
                } else {
                    callback(null)
                }
            }
        })
    }


    /**
     * 생활 리포트 - 식단 추천
     */
    fun requestRoutineRecommend(
        prompt: String,
        model: String = "text-davinci-003",
        maxTokens: Int = 1000,
        temperature: Double = 0.8,
        callback: (String?) -> Unit
    ) {
        val jsonObject = JSONObject()
        jsonObject.put("prompt", prompt)
        jsonObject.put("model", model)
        jsonObject.put("max_tokens", maxTokens)
        jsonObject.put("temperature", temperature)

        val requestBody =
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .post(requestBody)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("식단 추천 API 오류", "API 호출 중 오류 발생", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.e("GPT3 RESPONSE", responseData.toString())

                val jsonObject = JSONObject(responseData)
                val choicesArray = jsonObject.getJSONArray("choices")
                if (choicesArray.length() > 0) {
                    val choice = choicesArray.getJSONObject(0)
                    val text = choice.getString("text").trim()
                    callback(text)
                } else {
                    callback(null)
                }
            }
        })

    }

}