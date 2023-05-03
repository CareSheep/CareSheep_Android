package com.swu.caresheep.elder

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.swu.caresheep.R
import com.swu.caresheep.ui.elder.main.ElderActivity
import kotlinx.android.synthetic.main.activity_elder_voice_sub.*
import kotlinx.android.synthetic.main.activity_elder_voice_sub.voice_question
import java.io.File
import java.util.*

class ElderVoiceSubActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION  = 100 // 음성 녹음
    private val REQUEST_CODE_PERMISSIONS  = 200 // storage 권한
    private lateinit var recognizerIntent: Intent
    private var speechRecognizer : SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elder_voice_sub)

        // 권한 체크
        if (Build.VERSION.SDK_INT >= 23)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO),
                REQUEST_PERMISSION
            )

        // RecognizerIntent 생성
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }

        // 음성인식 기능 실행
        val mRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        mRecognizer.setRecognitionListener(listener)
        mRecognizer.startListening(intent)

        // 녹음 버튼 클릭 시, 음성인식 기능 종료
        record_button.setOnClickListener {
            speechRecognizer?.stopListening()
            Toast.makeText(applicationContext, "녹음이 종료되었습니다", Toast.LENGTH_SHORT).show()

            // 녹음이 종료되면 홈으로 이동
            val intent = Intent(this, ElderActivity::class.java)
            startActivity(intent)
        }

        // Storage 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS)
        }
    }


    private val listener: RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Toast.makeText(applicationContext, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show()
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            val message: String = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                else -> "알 수 없는 오류임"
            }

            Toast.makeText(applicationContext, "에러가 발생하였습니다. : $message", Toast.LENGTH_SHORT).show()
        }

        override fun onResults(results: Bundle?) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어준다.
            val matches: ArrayList<String>? =
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

            matches?.let {
                for (i in it.indices) {
                    voice_question.text = it[i]

                    // 파일로 저장하는 코드 추가
                    val fileName = "final_voice_recording_${System.currentTimeMillis()}.txt" //현재 시간을 밀리초 단위로 반환(중복없게)
                    val filePath = "${externalCacheDir?.absolutePath}/$fileName" // 저장 경로 /sdcard/android/com.swu.carsheep/cache/final_voice_recording_녹음시간.txt
                    val file = File(filePath)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    file.writeText(it[i])
                }
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

}