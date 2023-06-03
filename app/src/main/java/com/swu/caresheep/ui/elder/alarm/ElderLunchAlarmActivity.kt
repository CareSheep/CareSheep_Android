package com.swu.caresheep.ui.elder.alarm

import android.app.KeyguardManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_elder_lunch_alarm.lunch
import java.time.LocalDate
import java.util.Calendar

class ElderLunchAlarmActivity : AppCompatActivity() {

    private var flag = true
    private lateinit var calendar: Calendar

    // 데이터 베이스 연결
    private lateinit var dbRef: DatabaseReference

    //오늘의 날짜
    val todayDate: LocalDate = LocalDate.now()

    // 점심을 먹었는지 확인
    var done : Int = 0

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금일 때 화면 뜨도록
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_elder_lunch_alarm)

        // PowerManager 객체 생성
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        // PARTIAL_WAKE_LOCK을 사용하여 WakeLock 생성
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp:WakeLockTag"
        )

        // WakeLock 획득
        wakeLock?.acquire()

        // 라디오버튼
        lunch.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                // 점심 먹음 체크
                R.id.lunch_done -> {
                    done = 1
                    // 오늘의 날짜
                    val todayDate: LocalDate = LocalDate.now()

                    val data = hashMapOf(
                        "date" to todayDate.toString(),
                        "done" to done,
                        "user_id" to 1,
                    )

                    dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("Lunch")
                    dbRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val childCount = dataSnapshot.childrenCount
                            val id = (childCount + 1).toString()

                            val breakfastRef = dbRef.child(id)
                            breakfastRef.setValue(data)
                                .addOnSuccessListener {
                                    Log.d("점심 식사", "DB에 저장 성공")
                                }
                                .addOnFailureListener {
                                    Log.d("점심 식사", "DB에 저장 실패")
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("점심 식사", "Database error: ${error.message}")
                        }
                    })
                    finish()
                    flag = false

                }
                // 점심 안 먹음 체크
                R.id.lunch_no -> {
                    done = 0
                    // 오늘의 날짜
                    val todayDate: LocalDate = LocalDate.now()

                    val data = hashMapOf(
                        "date" to todayDate.toString(),
                        "done" to done,
                        "user_id" to 1,
                    )

                    dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("Lunch")
                    dbRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val childCount = dataSnapshot.childrenCount
                            val id = (childCount + 1).toString()

                            val breakfastRef = dbRef.child(id)
                            breakfastRef.setValue(data)
                                .addOnSuccessListener {
                                    Log.d("점심 식사", "DB에 저장 성공")
                                }
                                .addOnFailureListener {
                                    Log.d("점심 식사", "DB에 저장 실패")
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("점심 식사", "Database error: ${error.message}")
                        }
                    })
                    finish()
                    flag = false
                }

            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // WakeLock 해제
        wakeLock?.release()

        // MediaPlayer 정리
        mediaPlayer?.release()
        mediaPlayer = null

    }

    private fun checkLunch() {
        val data = hashMapOf(
            "date" to todayDate,
            "done" to done,
            "user_id" to 1,
        )

        dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("Lunch")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val childCount = dataSnapshot.childrenCount
                val id = (childCount + 1).toInt()
                dinner_id = id // 약 고유번호 정해주기 -> 다음 액티비티에서 사용

                dbRef.child(id.toString()).setValue(data)
                    .addOnSuccessListener {
                        Log.e("저녁 식사", "DB에 저장 성공")
                    }.addOnFailureListener {
                        Log.e("저녁 식사", "DB에 저장 실패")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("복약내용- 색상", "Database error: $error")
            }
        })
    }
}