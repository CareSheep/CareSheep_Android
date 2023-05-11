package com.swu.caresheep.elder

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.R
import com.swu.caresheep.Voice
import com.swu.caresheep.ui.elder.main.ElderActivity
import kotlinx.android.synthetic.main.activity_elder_voice_sub.*
import kotlinx.android.synthetic.main.activity_elder_voice_sub.voice_question
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class ElderVoiceSubActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION  = 100 // 음성 녹음
    private val REQUEST_CODE_PERMISSIONS  = 200 // storage 권한
    private lateinit var recognizerIntent: Intent
    private var speechRecognizer : SpeechRecognizer? = null

    private lateinit var database : DatabaseReference // DB (파이어베이스)


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

//            matches?.let {
//                for (i in it.indices) {
//                    voice_question.text = it[i]
//
//                    // 파일로 저장하는 코드 추가
//                    val fileName = "final_voice_recording_${System.currentTimeMillis()}.txt" //현재 시간을 밀리초 단위로 반환(중복없게)
//                    val filePath = "${externalCacheDir?.absolutePath}/$fileName" // 저장 경로 /sdcard/android/com.swu.carsheep/cache/final_voice_recording_녹음시간.txt
//                    val file = File(filePath)
//                    if (!file.exists()) {
//                        file.createNewFile()
//                    }
//                    file.writeText(it[i])
//                }
//                // 녹음이 종료되면 홈으로 이동
//                val intent = Intent(applicationContext, ElderActivity::class.java)
//                startActivity(intent)
//            }
            // val record_time: LocalDateTime = LocalDateTime.now() // 현재 날짜와 시간

            // 파일 이름 변수를 현재 날짜가 들어가도록 초기화. 그 이유는 중복된 이름으로 기존에 있던 파일이 덮어 쓰여지는 것을 방지하고자 함.
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

            matches?.let {
                // Firebase Realtime Database에 쓰기
                for (i in it.indices) {
                    voice_question.text = it[i] // 화면에 말하고 있는 문장 표시되도록
                    // Voice data class
                    val voice = Voice(
                        content = it[i],    // 음성을 텍스트로
                        recording_date = timeStamp,
                        // 우선 다 0으로(test)
                        danger = 0,
                        check = 0,
                        in_need = 0,
                        user_id = 1,
                        voice_id = 1
                    )

                    database = FirebaseDatabase.getInstance("https://caresheep-dcb96-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Voice")  //Voice 테이블에 접근
                    //database.push().setValue(voice)// 데이터가 계속 쌓이도록(임의 키 값으로 자동 생성)
                    database.child(timeStamp).setValue(voice)// 데이터가 계속 쌓이도록(timeStamp가 참조 꼬리로 쌓이도록)
                        //업로드 성공했는지 확인해보려고
                        .addOnSuccessListener {
                            Log.d("Firebase", "데이터 업로드 성공")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "데이터 업로드 실패: ${exception.message}", exception)
                        }
                }
                // 녹음이 종료 & 홈으로 이동
                speechRecognizer?.stopListening()
                val intent = Intent(applicationContext, ElderActivity::class.java)
                startActivity(intent)

            }

        }
        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }


}

