package com.swu.caresheep.utils

import android.Manifest
import android.app.*
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.swu.caresheep.ElderMapsActivity
import com.swu.caresheep.ElderMapsActivity.Companion.moveAppSettings
import com.swu.caresheep.R
import com.swu.caresheep.ui.elder.main.ElderActivity

const val NOTIFICATION_PERMISSION_REQUEST_CODE = 200

object NotificationManager {

    private const val CHANNEL_ID = "caresheep_gps_foreground_service_channel"

    fun initNotificationManager(activity: ElderMapsActivity) {
        // 알림 권한 확인
        if (!hasNotificationPermission(activity)) {
            // Android 13(SDK 33) 이상은 알림 권한 요청 필요
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            } else {
                if (LocationUtil.hasAllPermissions(activity)) {
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle("서비스 이용 알림").setCancelable(false)
                    builder.setMessage("앱을 사용하기 위해서는 알림 권한이 필요합니다. 설정으로 이동하여 권한을 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { _, _ ->
                        moveAppSettings(activity, NOTIFICATION_PERMISSION_REQUEST_CODE)
                    }
                    builder.show()
                } else {
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle("서비스 이용 알림").setCancelable(false)
                    builder.setMessage("앱을 사용하기 위해서는 위치, 알림 권한이 필요합니다. 설정으로 이동하여 권한을 항상 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { _, _ ->
                        moveAppSettings(activity, FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    builder.show()
                }
            }
        } else {
            if (LocationUtil.hasAllPermissions(activity) && !GpsForegroundService.isServiceRunning) {
                // 포그라운드 서비스 시작
                val serviceIntent = Intent(activity, GpsForegroundService::class.java)
                ContextCompat.startForegroundService(activity, serviceIntent)
            }
        }

    }

    fun createNotificationChannel(
        context: Context
    ) {
        // 포그라운드 서비스 알림 채널 생성
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.caresheep_gps_foreground_service_channel),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description =
                context.getString(R.string.caresheep_gps_foreground_service_channel_description)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun buildNotification(
        context: Context
    ): Notification {
        // 포그라운드 서비스 알림을 눌렀을 때 실행될 Intent 생성
        val notificationIntent = Intent(context, ElderActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 서비스 종료 버튼을 눌렀을 때 실행될 Intent 생성
        val stopIntent = Intent(context, GpsForegroundService::class.java)
        stopIntent.action = GpsForegroundService.ACTION_STOP_FOREGROUND_SERVICE
        val stopPendingIntent = PendingIntent.getService(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.caresheep_gps_foreground_service_channel))
            .setContentText(context.getString(R.string.caresheep_gps_foreground_service_channel_description))
            .setSmallIcon(R.drawable.img_app_icon)
            .setColor(ContextCompat.getColor(context, R.color.green))
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.img_app_icon, context.getString(R.string.caresheep_gps_foreground_service_cancel), stopPendingIntent)
            .setOngoing(true)
            .setVibrate(null)
            .setSound(null)
            .build()
    }

    // 알림 권한 허용 여부를 확인
    fun hasNotificationPermission(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }
}