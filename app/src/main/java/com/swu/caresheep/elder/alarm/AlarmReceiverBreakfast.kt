package com.swu.caresheep.elder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

class AlarmReceiverBreakfast : BroadcastReceiver() {

    companion object {
        const val ACTION_RESTART_SERVICE = "Restart"
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("AlarmReceive", "AlarmReceive")

        if (intent.action == ACTION_RESTART_SERVICE) {

            // 화면을 깨우기 위해 WakeLock 획득
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "MyApp:AlarmWakeLock"
            )
            wakeLock.acquire()

            val inte = Intent(context, AlarmServiceBreakfast::class.java)
            context.startService(inte)

        }


        //핸드폰 기본 알람 소리 재생
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioAttributes(audioAttributes)
        mediaPlayer?.setDataSource(context, Settings.System.DEFAULT_ALARM_ALERT_URI)
        mediaPlayer?.prepare()
        mediaPlayer?.start()

        // 일정 시간 후에 알람 소리 중지
        val stopDelayMillis = 5000 // 5초후에 중지
        val stopHandler = android.os.Handler()
        stopHandler.postDelayed({
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }, stopDelayMillis.toLong())


    }
}