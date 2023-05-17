package com.swu.caresheep.elder

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class AlarmService : Service() {
    private val TAG = "TAG+Service"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AlarmService")
        val alarmIntent = Intent(applicationContext, ElderMedicineFirstActivity::class.java)
        startActivity(alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return super.onStartCommand(intent, flags, startId)
    }
}