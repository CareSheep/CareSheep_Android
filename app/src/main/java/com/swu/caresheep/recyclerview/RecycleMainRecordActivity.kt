package com.swu.caresheep.recyclerview

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import com.swu.caresheep.Voice
import kotlinx.android.synthetic.main.activity_recycle_record_main.*

class RecycleMainRecordActivity : AppCompatActivity() {

    lateinit var recordAdapter: RecordAdapter


    //val datas = mutableListOf<RecordData>() // 테스트용 임시 데이터
    private val database = FirebaseDatabase.getInstance(DB_URL).getReference("Voice")  //Firebase DB의 Voice 테이블에 접근

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_record_main)

        initRecycler()
        loadData()

        // 등록 id 확인
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val fcmtoken = task.result
            Log.d(TAG, "FCM registration token: $fcmtoken")
        })
    }


//    // 서비스로부터 인텐트 받았을 때 처리
//    override fun onNewIntent(intent: Intent?) {
//        println("onNewIntent 호출됨")
//        intent?.let { processIntent(it) }
//        super.onNewIntent(intent)
//    }
//    private fun processIntent(intent: Intent) {
//        val from = intent.getStringExtra("from")
//        if (from == null) {
//            println("from is null.")
//            return
//        }
//    }

    private fun initRecycler() {// 리사이클러뷰와 어뎁터 초기화
        recordAdapter = RecordAdapter(this)
        rv_recorder.adapter = recordAdapter

    }
    private fun loadData() { //  Firebase Realtime Database에서 데이터를 가져와서 리사이클러뷰에 표시
        // 해당 경로의 데이터가 변경될 때마다 onDataChange() 콜백을 호출하여 데이터를 가져옴
        database.addValueEventListener(object : ValueEventListener {
            // 가져온 데이터를 RecordData 객체로 변환해서 recordList에 추가
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 리사이클러뷰에 표시할 데이터 리스트를 저장
                val recordList = mutableListOf<Voice>()

                for (snapshot in dataSnapshot.children.reversed()) { // db 저장된 역순(최신 것이 상위)
                    val record = snapshot.getValue(Voice::class.java)
                    record?.let {

                        recordList.add(it)
                    }
                }
                recordAdapter.datas = recordList
                recordAdapter.notifyDataSetChanged() // 리사이클러뷰 화면 갱신
            }

            override fun onCancelled(error: DatabaseError) {
                // 읽기 실패시 -> 예외 확인
                val exception = error.toException()
                Log.e("FirebaseDatabase", "Failed to read value: ${exception.message}", exception)

            }
        })
    }

}