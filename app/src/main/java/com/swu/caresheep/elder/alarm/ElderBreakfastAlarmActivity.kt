package com.swu.caresheep.elder.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
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
import com.swu.caresheep.utils.alarm.AlarmHandler
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import com.swu.caresheep.start.user_id
import kotlinx.android.synthetic.main.activity_elder_breakfast_alarm.breakfast
import java.time.LocalDate
import java.util.Calendar


var breakfast_id : Int = 0
// 알고리즘 순서
// 1. Routine 데이터 테이블에서 값들을 알람 매니저에 저장 = 이건 ElderConnedtActivity 혹은 ElderActivity에서 하는 것이 맞음
// 2. 알람 매니저에의해 뜨는 화면에서 ~하시겠습니까? - 예  버튼을 누를시  Breakfast 테이블에
// 오늘의 날짜, done, uer_id의 값이 저장

// 알람 매니저에 의해 뜨는 화면에 대한 Activity
class ElderBreakfastAlarmActivity : AppCompatActivity() {

    private var flag = true
    private lateinit var calendar: Calendar

    private val alarmHandler: AlarmHandler by lazy { AlarmHandler(this) }

    // 데이터 베이스 연결
    private lateinit var dbRef: DatabaseReference


    // 아침을 먹었는지 확인
    var done : Int = 0

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alarmType = "breakfast"

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

        setContentView(R.layout.activity_elder_breakfast_alarm)

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
        breakfast.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                // 아침 먹음 체크
                R.id.breakfast_done -> {
                    done = 1
                    // 오늘의 날짜
                    val todayDate: LocalDate = LocalDate.now()

                    val data = hashMapOf(
                        "date" to todayDate.toString(),
                        "done" to done,
                        "user_id" to user_id,
                    )

                    dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("Breakfast")
                    dbRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val childCount = dataSnapshot.childrenCount
                            val id = (childCount + 1).toString()

                            val breakfastRef = dbRef.child(id)
                            breakfastRef.setValue(data)
                                .addOnSuccessListener {
                                    Log.d("아침 식사", "DB에 저장 성공")
                                }
                                .addOnFailureListener {
                                    Log.d("아침 식사", "DB에 저장 실패")
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("아침 식사", "Database error: ${error.message}")
                        }
                    })

                    finish()
                    flag = false
                    val intent = Intent(this, PraiseActivity::class.java)
                    startActivity(intent)

                    // AlarmHandler를 사용하여 알람 취소
                    alarmHandler.cancelAlarm()

                }
                // 아침 안 먹음 체크
                R.id.breakfast_no -> {
                    done = 0
                    // 오늘의 날짜
                    val todayDate: LocalDate = LocalDate.now()

                    val data = hashMapOf(
                        "date" to todayDate.toString(),
                        "done" to done,
                        "user_id" to user_id,
                    )

                    dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("Breakfast")
                    dbRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val childCount = dataSnapshot.childrenCount
                            val id = (childCount + 1).toString()

                            val breakfastRef = dbRef.child(id)
                            breakfastRef.setValue(data)
                                .addOnSuccessListener {
                                    Log.d("아침 식사", "DB에 저장 성공")
                                }
                                .addOnFailureListener {
                                    Log.d("아침 식사", "DB에 저장 실패")
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("아침 식사", "Database error: ${error.message}")
                        }
                    })

                    finish()
                    flag = false

                    // AlarmHandler를 사용하여 알람 재설정
                    alarmHandler.setReAlarm()
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

}