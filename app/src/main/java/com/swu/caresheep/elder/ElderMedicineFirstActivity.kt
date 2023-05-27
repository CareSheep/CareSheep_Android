package com.swu.caresheep.elder

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.MedicineTime
import com.swu.caresheep.R
import com.swu.caresheep.Voice
import kotlinx.android.synthetic.main.activity_elder_medicine_first.*
import java.util.*


class ElderMedicineFirstActivity : AppCompatActivity() {
    private var flag = true
    private lateinit var calendar: Calendar


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

        setContentView(R.layout.activity_elder_medicine_first)

        // PowerManager 객체 생성
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        // PARTIAL_WAKE_LOCK을 사용하여 WakeLock 생성
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp:WakeLockTag"
        )

        // WakeLock 획득
        wakeLock?.acquire()

        // 약 정보에 맞도록 화면 구성(색깔)
        var color: String = ""

        val database =
            FirebaseDatabase.getInstance("https://caresheep-dcb96-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val reference = database.getReference("MedicineTime")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val medicineData = childSnapshot.getValue(MedicineTime::class.java)
                    color = medicineData!!.color
                    Log.d("Firebase", "시간 값: $color")

                    // color에 맞게 색깔 지정
                    when (color) {
                        "red" -> medicine_color.setBackgroundResource(R.color.my_red)
                        "blue" -> medicine_color.setBackgroundResource(R.color.my_blue)
                        "yellow" -> medicine_color.setBackgroundResource(R.color.my_yellow)
                        "green" -> medicine_color.setBackgroundResource(R.color.my_green)
                        "purple" -> medicine_color.setBackgroundResource(R.color.my_purple)
                        "brown" -> medicine_color.setBackgroundResource(R.color.my_brown)
                        "black" -> medicine_color.setBackgroundResource(R.color.my_black)
                    }

                    // 출력 문자열에 색깔 정보 포함하여 설정
                    medicine_alarm.text = "$color 색 약 먹을\n시간입니다."
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 가져오기가 실패한 경우 처리할 내용
                val exception = databaseError.toException()
                Log.e("FirebaseDatabase", "Failed to read value: ${exception.message}", exception)
            }
        })

        // 라디오버튼
        medicine.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                // 약 먹음 체크
                R.id.medicine_done -> {
                    finish()
                    flag = false
                }

//                // 약 먹지 않음 체크
//                R.id.medicine_no ->


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