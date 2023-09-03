package com.swu.caresheep.utils.voice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.swu.caresheep.R
import com.swu.caresheep.guardian.voice.RecycleMainRecordActivity


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "FMS"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken 호출됨 : $token")
    }

    // 푸시 메시지를 받았을 때 그 내용을 액티비티로 보내는 메서드
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        try{
            Log.d(TAG, "onMessageReceived 호출됨.")
            val data: Map<String, String> = remoteMessage.data // 메시지 전송 시 넣은 데이터 확인
            val contents = data["contents"]

            sendToActivity(applicationContext, contents)
        }catch(e:Exception){
            Log.e(TAG, "Error in onMessageReceived: ${e.message}", e)
        }
    }

    // 액티비티로 데이터 보내기 위해 인텐트 객체 생성 후 startActivity() 메서드 호출
    private fun sendToActivity(context: Context, contents: String?) {
        try { // 푸시 알림
            val intent = Intent(context, RecycleMainRecordActivity::class.java)
            intent.putExtra("contents", contents)
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
                .setContentTitle("어르신 음성")
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