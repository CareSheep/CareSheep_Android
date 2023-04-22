package com.swu.caresheep.elder

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_elder_voice_main.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class ElderVoiceMainActivity : AppCompatActivity() {

//    // Google Cloud Storage 클라이언트 객체 생성
//    var credentials = GoogleCredentials.fromStream(FileInputStream("app/caresheep-3eca58442fae.json"))
//    var storage: Storage = StorageOptions.newBuilder().setCredentials(credentials).build().service

    // 오디오 녹음
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var mRecorder: MediaRecorder? = null
    private var mOutputFile: String? = null
    private var isRecording = false
    private val audioUri: Uri? = null // 오디오 파일 uri

    private val audioFile: File? = null// 앱의 전용 디렉토리에 저장되는 오디오 파일

    //    object Constants {
//        const val BUCKET_NAME = "caresheep_file"
//    }
    // 녹음 권한 확인 (없으면 사용자에게 녹음 권한 요청)
    private fun checkPermission() {
        permissionToRecordAccepted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionToRecordAccepted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elder_voice_main)

        // 녹음 권한 확인 함수 호출
        checkPermission()

        // 녹음 버튼이 클릭될 때마다 녹음 시작하거나 종료
        record_button.setOnClickListener {
            if (isRecording) {
                stopRecording()
                record_button.text = "record" // 텍스트 변경하여 녹음 상태 시각화
                //uploadRecording(File(mOutputFile!!)) // 녹음이 끝나면 업로드
                //voice_no.visibility = View.INVISIBLE // 아니오 버튼 안보이게 (고민중: 아니면 아니오 대신 녹음중입니다로 텍스트 바뀌게 할지?)
            } else {
                startRecording()
                record_button.text = "Stop"
            }
            isRecording = !isRecording
        }
    }
    //val filePath = getExternalFilesDir(null)?.absolutePath + "/files/hello_world.mp3"


//
//    //녹음 시작
//    private fun startRecording() {
//        val currentDate = SimpleDateFormat(
//            "yyyyMMdd_HHmmss",
//            Locale.getDefault()
//        ).format(Date()) // 현재 날짜와 시간을 문자열로 변환
//
//        // 디렉토리 생성
//        val directory = File("${externalCacheDir?.absolutePath}/recordings/")
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
//
//        // 파일 경로 설정
//        mOutputFile = "${directory.absolutePath}/recording_$currentDate.mp3"
//
////        mOutputFile = Environment.getExternalStorageDirectory().getAbsolutePath();
////        mOutputFile += "/recording_$currentDate.mp3" // 파일 이름 설정
//
//        mRecorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC) // 녹음할 오디오 소스 설정
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // 출력 파일 형식 설정
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC) // 오디오 인코더 설정
//            setOutputFile(mOutputFile) //출력 파일 경로 설정
////            prepare()
////            start()
//        }
//        try {
//            mRecorder?.prepare() //초기화를 완료
//        } catch (e: IOException) {
//            Log.e(TAG, "prepare() failed")
//            return
//        }
//        mRecorder?.start() //녹음기를 시작
//    }


    // 녹음 시작
    private fun startRecording() {
        //파일의 외부 경로 확인
        val recordPath = getExternalFilesDir("/")!!.absolutePath
        // 파일 이름 변수를 현재 날짜가 들어가도록 초기화. 그 이유는 중복된 이름으로 기존에 있던 파일이 덮어 쓰여지는 것을 방지하고자 함.
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        mOutputFile = recordPath + "/" + "RecordExample_" + timeStamp + "_" + "audio.mp4"

        //Media Recorder 생성 및 설정
        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mRecorder!!.setOutputFile(mOutputFile)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            mRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //녹음 시작
        mRecorder!!.start()
    }


    // 녹음 종료
    private fun stopRecording() {
        mRecorder?.apply {
            stop()
            release() // MediaRecorder 객체 해제
        }
        mRecorder = null

        // 녹음이 종료되면 파일 업로드 수행
        uploadRecording(File(mOutputFile!!))
    }


    //녹음 파일 업로드
//    private fun uploadRecording(file: File) {
//        Executors.newSingleThreadExecutor().submit {
//            try {
//                // Google Cloud Storage 인증
//                val credentials = GoogleCredentials.fromStream(resources.openRawResource(R.raw.credentials))
//                val storage = StorageOptions.newBuilder().setCredentials(credentials).build().service
//                // 버킷 정보
//                val bucketName = "caresheep_file"
//                val blobInfo = BlobInfo.newBuilder(bucketName, "recordings/${file.name}").build()
//
//                // 파일 업로드
//                storage.create(blobInfo, file.inputStream())
//
//                // 업로드 성공 시 처리할 작업
//                val storageUri = "gs://$bucketName/${blobInfo.name}"
//                Log.d("TAG", "File uploaded successfully: $storageUri")
//
//            } catch (e: Exception) {
//                // 업로드 실패 시 처리할 작업
//                Log.e("TAG", "File upload failed: ${e.message}")
//            }
//        }
//    }

    private fun uploadRecording(file: File) {
        val credentials = GoogleCredentials.fromStream(resources.openRawResource(R.raw.credentials))
        val storage = StorageOptions.newBuilder().setCredentials(credentials).build().service

        val bucketName = "caresheep_file" // 여러분이 생성한 버킷 이름으로 변경해주세요.
        val blobName = file.name

        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, blobName)).build()
        storage.create(blobInfo, Files.readAllBytes(file.toPath()))

        Log.d("TAG", "Uploaded file: $blobName")

        // RecognitionAudio 객체 생성
        val audioUri = "gs://$bucketName/$blobName"
        val audio = RecognitionAudio.newBuilder()
            .setUri(audioUri)
            .build()

        val credential = GoogleCredentials.fromStream(resources.openRawResource(R.raw.stt))
        val settings = SpeechSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credential))
            .build()

        // RecognitionConfig 객체 생성
        val config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED)
            .setLanguageCode("ko-KR")
            .build()

        val speechClient = SpeechClient.create(settings)

        // Speech-to-Text API에 RecognitionConfig와 RecognitionAudio 전송
        val response = speechClient.recognize(config, audio)

        // API 응답 처리
        val results = response.resultsList
        if (results.isNotEmpty()) {
            val result = results[0]
            val transcript = result.alternativesList[0].transcript
            Log.d(TAG, "Transcription: $transcript")
            saveToFile(transcript)
        } else {
            Toast.makeText(this, "음성을 텍스트로 변환할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    // 텍스트로 된 저장
    private fun saveToFile(transcript: String) {
        val file = File(filesDir, "output.txt")
        file.writeText(transcript)
        Toast.makeText(this, "파일이 저장되었습니다.", Toast.LENGTH_SHORT).show()
    }
//    fun uploadToCloudStorage(bucketName: String, filePath: String, fileName: String) {
//        val storage: Storage = StorageOptions.newBuilder().setProjectId("caresheep").build().service
//        val blobId = BlobId.of(bucketName, fileName)
//        val blobInfo = storage.create(
//            BlobInfo.newBuilder(blobId)
//                .setContentType("audio/mpeg")
//                .build(),
//            FileInputStream(filePath)
//        )
//        println("File $fileName uploaded to bucket $bucketName as ${blobInfo.mediaLink}")
//    }

//    private val speechClient: SpeechClient by lazy {
//        applicationContext?.resources?.openRawResource(R.raw.stt)?.let { inputStream ->
//            SpeechClient.create(
//                SpeechSettings.newBuilder()
//                    .setCredentialsProvider { GoogleCredentials.fromStream(inputStream) }
//                    .build())
//        }
//    }



}



