package com.swu.caresheep.utils.map

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat

class GpsForegroundService : Service() {

    companion object {
        const val GPS_FOREGROUND_NOTIFICATION_ID = 1
        const val ACTION_STOP_FOREGROUND_SERVICE = "stop"
        var isServiceRunning = false  // 서비스 진행 여부
    }

    private val locationProviderReceiver = object : BroadcastReceiver() {
        private var isGpsEnabled: Boolean = false
        private var isNetworkEnabled: Boolean = false

        override fun onReceive(context: Context, intent: Intent) {
            intent.action?.let { action ->
                if (action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    val locationManager =
                        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    isNetworkEnabled =
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                    // 위치 제공자가 비활성화되면
                    if (!isGpsEnabled || !isNetworkEnabled) {
                        // 포그라운드 서비스 종료
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        // 위치 서비스 상태 변경 브로드캐스트 리시버 등록
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(locationProviderReceiver, filter)

        // 포그라운드 서비스 알림 채널 생성
        NotificationManager.createNotificationChannel(this)

        // 포그라운드 서비스 알림 생성
        val notification = NotificationManager.buildNotification(this)

        // 포그라운드 서비스 시작
        startForeground(GPS_FOREGROUND_NOTIFICATION_ID, notification)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning = true

        // 실시간 위치 업데이트 시작
        LocationUtil.startLocationUpdates()

        if (intent == null) {
            return START_NOT_STICKY
        }
        if (intent.action == ACTION_STOP_FOREGROUND_SERVICE) {
            // 포그라운드 서비스 종료
            NotificationManagerCompat.from(this).cancel(GPS_FOREGROUND_NOTIFICATION_ID)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelfResult(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false

        NotificationManagerCompat.from(this).cancel(GPS_FOREGROUND_NOTIFICATION_ID)
        // BroadcastReceiver 등록 해제
        unregisterReceiver(locationProviderReceiver)
        LocationUtil.stopLocationUpdates()
    }

}