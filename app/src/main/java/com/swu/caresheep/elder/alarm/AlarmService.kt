package com.swu.caresheep.elder.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log

var alarmType = ""
class AlarmService : Service() {
    private val TAG = "TAG+Service"

    private var wakeLock: PowerManager.WakeLock? = null


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AlarmService")

        // PowerManager 객체 생성
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        // PARTIAL_WAKE_LOCK을 사용하여 WakeLock 생성
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp:WakeLockTag"
        )

        // WakeLock 획득
        wakeLock?.acquire()

        var alarmIntent = Intent(applicationContext, ElderMedicineFirstActivity::class.java)
        // 알람 화면 실행
        if(alarmType == "breakfast"){
            alarmIntent = Intent(applicationContext, ElderBreakfastAlarmActivity::class.java)
            alarmType == "" // 초기화
        }else if(alarmType == "lunch"){
            alarmIntent = Intent(applicationContext, ElderLunchAlarmActivity::class.java)
            alarmType == "" // 초기화
        }else if(alarmType == "dinner"){
            alarmIntent = Intent(applicationContext, ElderDinnerAlarmActivity::class.java)
            alarmType == "" // 초기화
        }else if(alarmType == "medicine"){
            alarmIntent = Intent(applicationContext, ElderMedicineFirstActivity::class.java)
            alarmType == "" // 초기화
        }


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