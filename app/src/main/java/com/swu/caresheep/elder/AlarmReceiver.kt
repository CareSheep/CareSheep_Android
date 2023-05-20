package com.swu.caresheep.elder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var pm: PowerManager
    private lateinit var wl: PowerManager.WakeLock

    companion object {
        const val ACTION_RESTART_SERVICE = "Restart"
    }


    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == ACTION_RESTART_SERVICE) {
            pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyApp:AlarmTest")
            wl.acquire(60 * 1000L /*1 minute*/)

            val inte = Intent(context, AlarmService::class.java)
            context.startService(inte)
        }
        wl.release()

         //basic notification channel
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = context.getString(R.string.high_importance_channel_name)
//            val descriptionText = context.getString(R.string.high_importance_channel_desc)
//            // 중요 부분. importance high로 설정해야 한다!
//            val importance = NotificationManager.IMPORTANCE_HIGH
//            val channel = NotificationChannel(
//                context.getString(R.string.high_noti_channel_id),
//                name,
//                importance
//            ).apply {
//                description = descriptionText
//            }
//            val notificationManager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//
////        val intent = Intent(context, NotiSampleActivity::class.java).apply {
////            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
////        }
//        val pendingIntent = PendingIntent.getActivity(context, 0,intent, 0)
//// fullscreen 용 Activity Intent 생성
//        val fullscreenIntent = Intent(context, ElderMedicineFirstActivity::class.java).apply {
//            action = "fullscreen_activity"
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val fullscreenPendingIntent = PendingIntent.getActivity(context, 0, fullscreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        val builder = NotificationCompat.Builder(context, context.getString(R.string.high_noti_channel_id)).apply {
//            setSmallIcon(R.drawable.ic_launcher_foreground)
//            setContentTitle("fullscreen intent notification")
//            setContentText("fullscreen intent notification!")
//            setAutoCancel(true)
//            setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
//            setCategory(NotificationCompat.CATEGORY_ALARM)
//            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            setLocalOnly(true)
//            priority = NotificationCompat.PRIORITY_MAX
//            setContentIntent(fullscreenPendingIntent)
//            // <-- set full screen intent
//            setFullScreenIntent(fullscreenPendingIntent, true)
//        }
//        with(NotificationManagerCompat.from(context)) {
//            // notificationId is a unique int for each notification that you must define
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return@with
//            }
//            notify(0, builder.build())
//        }
        /////




//
//        // 알람이 발생했을 때 실행될 코드를 작성
//        // 예시: ElderMedicineFirstActivity를 띄우는 코드
//        val fullScreenIntent = Intent(context, ElderMedicineFirstActivity::class.java)
//        fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val fullScreenPendingIntent = PendingIntent.getActivity(context, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        val permission = Manifest.permission.USE_FULL_SCREEN_INTENT
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
//            context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
//            // 권한이 없을 경우 처리하는 코드
//            return
//        }
//
//        val builder = NotificationCompat.Builder(context, "my_channel_id")
//            .setSmallIcon(R.drawable.medicine)
//            .setContentTitle("My notification")
//            .setContentText("Hello World!")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setFullScreenIntent(fullScreenPendingIntent, true)
//
//        with(NotificationManagerCompat.from(context)) {
//            // notificationId is a unique int for each notification that you must define
//            notify(0, builder.build())
//        }
    }
}
