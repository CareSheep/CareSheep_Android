package com.swu.caresheep.elder

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_elder_medicine_first.*
import java.util.*


class ElderMedicineFirstActivity : AppCompatActivity() {
    private var flag = true
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var powerManager: PowerManager? = null
        var wakeLock: PowerManager.WakeLock? = null

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager?
        when {
            android.os.Build.VERSION.SDK_INT >= 27 -> {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                keyguardManager?.requestDismissKeyguard(this, null)

            }
            android.os.Build.VERSION.SDK_INT == 26 -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                keyguardManager?.requestDismissKeyguard(this, null)
            }
            else -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(true)
//            //setTurnScreenOn(true)
//            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//            keyguardManager.requestDismissKeyguard(this, null)
//        } else {
//            this.window.addFlags(
//                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
//                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
//        }


        //시도1 안됨
//        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager?
//        when {
//            Build.VERSION.SDK_INT >= 27 -> {
//                setShowWhenLocked(true)
//                setTurnScreenOn(true)
//                keyguardManager?.requestDismissKeyguard(this, null)
//            }
//            Build.VERSION.SDK_INT == 26 -> {
//                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
//                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
//                keyguardManager?.requestDismissKeyguard(this, null)
//            }
//            else -> {
//                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
//                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
//                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
//            }
//        }
//
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(true)
//            setTurnScreenOn(true)
//            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//            keyguardManager.requestDismissKeyguard(this, null)
//        } else {
//            this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
//                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(true)
//            setTurnScreenOn(true)
//            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
//        } else {
//            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED    // deprecated api 27
//                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD     // deprecated api 26
//                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON   // deprecated api 27
//                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
//        }
//        val keyguardMgr = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            keyguardMgr.requestDismissKeyguard(this, null)
//        }

        setContentView(R.layout.activity_elder_medicine_first)

        calendar = Calendar.getInstance()

//        // 알람을 설정할 시간
//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = System.currentTimeMillis()
//        calendar.add(Calendar.MINUTE, 3)
//
//// AlarmManager 생성
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
//        // 에러 확인
//        val alarmClockInfo = alarmManager.getNextAlarmClock()
//
//        if (alarmClockInfo != null) {
//            Log.d("Alarm", "알람Alarm됨${alarmClockInfo.triggerTime}")
//        } else {
//            Log.d("Alarm", "알람Alarm안됨")
//        }
//        //
//
//// 알람 화면을 띄우기 위한 코드
//        val fullScreenIntent = Intent(this, ElderMedicineFirstActivity::class.java)
//        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//// 알람 설정
//        alarmManager.setExact(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            fullScreenPendingIntent
//        )
//
//
//        // 로그를 출력하여 알람 시간이 제대로 설정되었는지 확인합니다.
//        Log.d("Alarm", "Alarm will ring at $calendar (UTC)")
//

//        window.addFlags(
//            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)


//        Thread {
//            while (flag == true) {
//                try {
//                    calendar = Calendar.getInstance()
//                    if (calendar.get(Calendar.HOUR_OF_DAY) > 0 && calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                        medicine_alarm.text = "AM ${calendar.get(Calendar.HOUR_OF_DAY)}시 ${calendar.get(Calendar.MINUTE)}분 ${calendar.get(Calendar.SECOND)}초"
//                    } else if (calendar.get(Calendar.HOUR_OF_DAY) == 12) {
//                        medicine_alarm.text = "PM ${calendar.get(Calendar.HOUR_OF_DAY)}시 ${calendar.get(Calendar.MINUTE)}분 ${calendar.get(Calendar.SECOND)}초"
//                    } else if (calendar.get(Calendar.HOUR_OF_DAY) > 12 && calendar.get(Calendar.HOUR_OF_DAY) < 24) {
//                        medicine_alarm.text = "PM ${(calendar.get(Calendar.HOUR_OF_DAY) - 12)}시 ${calendar.get(Calendar.MINUTE)}분 ${calendar.get(Calendar.SECOND)}초"
//                    } else if (calendar.get(Calendar.HOUR_OF_DAY) == 0) {
//                        medicine_alarm.text = "AM 0시 ${calendar.get(Calendar.MINUTE)}분 ${calendar.get(Calendar.SECOND)}초"
//                    }
//                    Thread.sleep(1000)
//                } catch (ex: InterruptedException) {
//                }
//            }
//        }.start()


     // 라디오버튼
        medicine.setOnCheckedChangeListener { radioGroup, checkedId ->
            when(checkedId) {
                // 약 먹음 체크
                R.id.medicine_done ->  {
                    finish()
                    flag = false
                    wakeLock?.release()
                }
//
//                // 약 먹지 않음 체크
//                R.id.medicine_no ->


            }
        }

    }
}