package com.swu.caresheep.elder

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.swu.caresheep.ui.elder.alarm.ElderWalkAlarmActivity

class AlarmServiceWalk : Service() {
    private val TAG = "TAG+Service"

    private var wakeLock: PowerManager.WakeLock? = null


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmService", "AlarmService")

        // PowerManager 객체 생성
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        // PARTIAL_WAKE_LOCK을 사용하여 WakeLock 생성
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp:WakeLockTag"
        )

        // WakeLock 획득
        wakeLock?.acquire()

        // 알람 화면 실행
        val alarmIntent = Intent(applicationContext, ElderWalkAlarmActivity::class.java)
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(alarmIntent)

        return START_STICKY

    }
    override fun onDestroy() {
        super.onDestroy()

        // WakeLock 해제
        wakeLock?.release()
    }
}