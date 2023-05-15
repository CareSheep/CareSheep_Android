package com.swu.caresheep

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

const val API_KEY = BuildConfig.API_KEY
const val API_URL = "https://api.openai.com/v1/completions"

class Gpt3Api {
    companion object {

        fun requestGpt3Api(
            prompt: String,
            model: String,
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
    }
}