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
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.Gpt3Api
import com.swu.caresheep.R
import com.swu.caresheep.Voice
import com.swu.caresheep.ui.elder.main.ElderActivity
import kotlinx.android.synthetic.main.activity_elder_voice_sub.voice_question
import java.text.SimpleDateFormat
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

            // 파일 이름 변수를 현재 날짜가 들어가도록 초기화. (이유: 중복된 이름으로 기존에 있던 파일이 덮어 쓰여지는 것을 방지하고자)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

            // 상황 분석에 필요한 변수  (default 0)
            var danger = "0" // 위험
            var in_need = "0" // 물건


            // 변환된 텍스트를 담을 리스트 선언
            val detectedWords = mutableListOf<String>()

            matches?.let {
                for (i in it.indices) {
                    // 단어를 리스트에 추가
                    detectedWords.add(it[i])
                    // 어르신 화면에 말하고 있는 문장 표시되도록
                    voice_question.text = it[i]
                }
            }

            // 리스트에 있는 단어들을 하나의 문자열로 합치기(옵션을 줘서 공백이 적절하게 추가된 문자열이 생성)
            val content = detectedWords.joinToString(separator = " ", prefix = "", postfix = "")


            // 모델에 content를 입력하고 위험 및 생필품 부족 여부를 판단한 후 값을 설정
            val model = "text-davinci-002"
            val prompt = """
                    다음 텍스트가 노인의 위험상황 및 물건 부족 상황인지 판단하세요:
                    $content
                    위험상황일 경우 '1', 그렇지 않을 경우 '0'을 입력하세요:
                    물건 부족 상황일 경우 '1', 그렇지 않을 경우 '0'을 입력하세요:
                    """.trimIndent()
            Gpt3Api.requestGpt3Api(prompt, model) { response -> // 요청
                // response에는 API 응답 결과가 반환됨
                val labels = response?.toString()?.split('\n')  // \n 기준으로 분할해서 labels 리스트에 저장 (null 처리하려고 ?.)
                val dangerLabel = labels?.get(0).toString() // 위험 상황이면 1
                val shortageLabel = labels?.get(1).toString() // 물건 부족 상황이면 1

                if(dangerLabel == "1") {
                    danger = "1"
                }
                if(shortageLabel == "1") {
                    in_need = "1"
                }

                // Voice의 각 필드에 넣기
                val voice = Voice(
                    content = content,
                    recording_date = timeStamp,
                    danger = danger,

                    in_need = in_need,

                    // 우선 디폴트 값으로
                    check = 0,
                    user_id = 1,

                    voice_id = 1
                )

                database = FirebaseDatabase.getInstance(DB_URL).getReference("Voice") //Voice 테이블에 접근
                database.child(timeStamp).setValue(voice)   // 데이터가 계속 쌓이도록(timeStamp가 참조 꼬리로 쌓이도록)
                    //업로드 성공했는지 확인해보려고
                    .addOnSuccessListener {
                        Log.d("Firebase", "데이터 업로드 성공")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "데이터 업로드 실패: ${exception.message}", exception)
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

