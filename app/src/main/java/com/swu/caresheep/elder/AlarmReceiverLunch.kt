package com.swu.caresheep.elder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import android.provider.Settings

class AlarmReceiverLunch : BroadcastReceiver() {

    companion object {
        const val ACTION_RESTART_SERVICE = "Restart"
    }


    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == ACTION_RESTART_SERVICE) {

            // 화면을 깨우기 위해 WakeLock 획득
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "MyApp:AlarmWakeLock"
            )
            wakeLock.acquire()

            val inte = Intent(context, AlarmServiceLunch::class.java)
            context.startService(inte)
        }


        //핸드폰 기본 알람 소리 재생
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioAttributes(audioAttributes)
        mediaPlayer.setDataSource(context, Settings.System.DEFAULT_ALARM_ALERT_URI)
        mediaPlayer.prepare()
        mediaPlayer.start()

    }
}