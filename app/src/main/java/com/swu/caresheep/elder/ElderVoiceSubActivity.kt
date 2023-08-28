package com.swu.caresheep.elder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
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
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.swu.caresheep.*
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.ui.elder.main.ElderActivity
import com.swu.caresheep.ui.start.user_id
import kotlinx.android.synthetic.main.activity_elder_voice_sub.voice_question
import java.text.SimpleDateFormat
import java.util.*
import com.swu.caresheep.MyFirebaseMessagingService
import com.swu.caresheep.R
import com.swu.caresheep.data.model.Guardian
import com.swu.caresheep.recyclerview.RecycleMainRecordActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException


class ElderVoiceSubActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION = 100 // 음성 녹음
    private val REQUEST_CODE_PERMISSIONS = 200 // storage 권한
    private lateinit var recognizerIntent: Intent
    private var speechRecognizer: SpeechRecognizer? = null

    private lateinit var database: DatabaseReference // DB (파이어베이스)

    private var fcmMessageTitle = "title"
    private var notificationTitle = "title"

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
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSIONS
            )
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
                    다음 텍스트가 노인이 위험한 상황인지 아니면 물건이 부족한 상황인지 판단하세요:
                    $content
                    노인이 위험한 상황일 경우 '1', 그렇지 않을 경우 '0'을 입력하세요:
                    물건이 부족해서 물건을 구매해야 할 상황일 경우 '1', 그렇지 않을 경우 '0'을 입력하세요:
                    """.trimIndent()
            Gpt3Api.requestGpt3Api(prompt, model) { response -> // 요청
                // response에는 API 응답 결과가 반환됨
                val labels = response?.toString()
                    ?.split('\n')  // \n 기준으로 분할해서 labels 리스트에 저장 (null 처리하려고 ?.)
                val dangerLabel = labels?.get(0).toString() // 위험 상황이면 1
//                val shortageLabel = labels?.get(1).toString() // 물건 부족 상황이면 1

                if (dangerLabel == "1") {
                    danger = "1"
                }
//                if (shortageLabel == "1") {
//                    in_need = "1"
//                }

                // Voice의 각 필드에 넣기
                val voice = Voice(
                    content = content,
                    recording_date = timeStamp,
                    danger = danger,

                    in_need = in_need,
                    user_id = 1,

                    // 우선 디폴트 값으로
                    check = 0,
                    voice_id = 1
                )

                database =
                    FirebaseDatabase.getInstance(DB_URL).getReference("Voice") //Voice 테이블에 접근
                database.child(timeStamp).setValue(voice)   // 데이터가 계속 쌓이도록(timeStamp가 참조 꼬리로 쌓이도록)
                    //업로드 성공했는지 확인해보려고
                    .addOnSuccessListener {
                        Log.d("Firebase", "데이터 업로드 성공")

                        sendPushNotificationToGuardians(content)

                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "데이터 업로드 실패: ${exception.message}", exception)
                    }

                // 녹음이 종료 & 홈으로 이동
                speechRecognizer?.stopListening()
                val intent = Intent(applicationContext, ElderActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
//
//            sendFCMNotification(danger, in_need, content) // fcm 알림 전송 메서드 호출
            var notificationTitle = ""
            if (danger == "1" && in_need == "1")
                notificationTitle = "danger and in_need"
            else if (danger == "1")
                notificationTitle = "danger"
            else if (in_need == "1")
                notificationTitle = "in_need"
            else if (danger == "0" && in_need == "0")
                notificationTitle = "daily"

            val notificationBody = content
            //MyFirebaseMessagingService().sendNotification(notificationTitle, notificationBody)

        }


        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun sendPushNotificationToGuardians(content: String) {
        val notificationData = mapOf(
            "title" to "New Voice Recording",
            "message" to content
        )

        // Retrieve guardian FCM tokens and send push notification
        database = FirebaseDatabase.getInstance(DB_URL).getReference("Guardian")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (guardianSnapshot in dataSnapshot.children) {
                    val guardian = guardianSnapshot.getValue(Guardian::class.java)
                    val guardianFCMToken = guardian?.fcmToken.toString()

                    sendPushNotification(guardianFCMToken, notificationData)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Guardian data fetch failed: ${error.message}")
            }
        })
    }

    private fun sendPushNotification(fcmToken: String, data: Map<String, String>) {
        val JSON = "application/json; charset=utf-8".toMediaType()

        val json = JSONObject(data)

        val requestBody = RequestBody.create(JSON, json.toString())

        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .header("Authorization", "Bearer $fcmToken")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("Firebase", "Push notification sent successfully")
            }
        })
    }

//
//    // fcm 알림 전송
//    private fun sendFCMNotification(danger: String, in_need: String, content: String) {
//        val notificationManager: NotificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // 채널 생성
//        val channel = NotificationChannel(
//            "fcm_default_channel",
//            "음성 알림",
//            NotificationManager.IMPORTANCE_HIGH
//        )
//        notificationManager.createNotificationChannel(channel)
//
//        // fcm 알림 제목 설정
//        if(danger == "1" && in_need == "1")
//            fcmMessageTitle = "danger and in_need"
//        else if (danger == "1")
//            fcmMessageTitle = "danger"
//        else if (in_need == "1")
//            fcmMessageTitle = "in_need"
//        else if (danger == "0" && in_need == "0")
//            fcmMessageTitle = "daily"
//
//        val fcmMessageContent = content
//
//        // fcm 알림 클릭 시 이동될 화면
////        val intent = Intent(this, RecycleMainRecordActivity::class.java)
////        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
////        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
//
//
////        val notification = NotificationCompat.Builder(this, "fcm_default_channel")
////            .setContentTitle(fcmMessageTitle)
////            .setContentText(fcmMessageContent)
////            .setSmallIcon(R.drawable.ic_notification)
////            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
////            .setContentIntent(pendingIntent) // 이동 화면
////            .setAutoCancel(true) //클릭 시 알림 사라지도록
////            .build()
//
//        // Guardian 정보를 Firebase에서 가져오는 작업
//        database =
//            FirebaseDatabase.getInstance(DB_URL).getReference("Guardian")
//        database.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                var guardianFCMToken = "" // Guardian의 FCM 토큰
//                for (guardianSnapshot in dataSnapshot.children) {
//                    val guardian = guardianSnapshot.getValue(Guardian::class.java)
//                    guardianFCMToken = guardian?.fcmToken.toString()
//
//                    if (!guardianFCMToken.isNullOrEmpty()) {
//                        // Guardian의 토큰이 존재하면 FCM 알림 전송
//                        val data = mapOf(
//                            "title" to fcmMessageTitle,
//                            "message" to fcmMessageContent
//                        )
//
//                        // Create the notification message
//                        val remoteMessage = RemoteMessage.Builder(guardianFCMToken)
//                            .setData(data)
//                            .build()
//
//                        FirebaseMessaging.getInstance().send(remoteMessage)
//                    }
//
//                }
//
//            }
//
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // 에러 처리
//                Log.e("Firebase", "Error fetching data: ${databaseError.message}")
//            }
//        })
//
//
//    }


}