package com.swu.caresheep.data.api

import android.os.AsyncTask
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.swu.caresheep.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

const val KAKAO_IMG_API = BuildConfig.KAKAO_IMG_API

class KakaoImgSearchApi(private val textView: TextView, private val imageView: ImageView) {

    fun searchImage(query: String) {
        KakaoImageSearchTask(textView, imageView).execute(query)
    }

    private class KakaoImageSearchTask(
        private val textView: TextView,
        private val imageView: ImageView
    ) : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String {
            val query = params[0]

            // Kakao API 호출
            val url = URL("https://dapi.kakao.com/v2/search/image?query=$query")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "KakaoAK $KAKAO_IMG_API")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                return response.toString()
            }

            return "Error: $responseCode"
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            // 파싱하여 이미지 URL 획득 및 표시
            val jsonObject = JSONObject(result)
            val documents = jsonObject.getJSONArray("documents")

            if (documents.length() > 0) {
                val imageUrl = documents.getJSONObject(0).getString("image_url")
                loadAndDisplayImage(imageUrl)
            }
        }

        private fun loadAndDisplayImage(imageUrl: String) {
            // Glide 라이브러리를 사용하여 이미지 로딩 및 표시
            Glide.with(textView.context)
                .load(imageUrl)
                .into(imageView)
        }
    }
}