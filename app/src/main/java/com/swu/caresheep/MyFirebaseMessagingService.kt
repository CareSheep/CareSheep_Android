package com.swu.caresheep

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.swu.caresheep.recyclerview.RecycleMainRecordActivity


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "FMS"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken 호출됨 : $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        try{
            Log.d(TAG, "onMessageReceived 호출됨.")
            val from = remoteMessage.from
            val data: Map<String, String> = remoteMessage.data
            val contents = data["contents"]
            Log.d(TAG, "from : $from, contents : $contents")
            sendToActivity(applicationContext, from, contents)
        }catch(e:Exception){

            Log.e(TAG, "Error in onMessageReceived: ${e.message}", e)
        }

    }

    private fun sendToActivity(context: Context, from: String?, contents: String?) {
        try { // 푸시 알림
            val intent = Intent(context, RecycleMainRecordActivity::class.java)
            intent.putExtra("from", from)
            intent.putExtra("contents", contents)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 채널 생성
            val channel = NotificationChannel(
                "fcm_default_channel",
                "음성 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
            }
            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, "fcm_default_channel")
                .setContentTitle("FCM Title")
                .setContentText(contents)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 알림 클릭 시 전환할 화면
                .setAutoCancel(true) // 클릭하면 알림 사라지게
                .build()

            notificationManager.notify(1, notification)

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendToActivity: ${e.message}", e)
        }
    }
}
