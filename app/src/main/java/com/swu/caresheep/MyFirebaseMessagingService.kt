package org.techtown.push

import android.content.Context
import android.content.Intent
import android.util.Log
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
        Log.d(TAG, "onMessageReceived 호출됨.")
        val from = remoteMessage.from
        val data: Map<String, String> = remoteMessage.data
        val contents = data["contents"]
        Log.d(TAG, "from : $from, contents : $contents")
        sendToActivity(applicationContext, from, contents)
    }

    private fun sendToActivity(context: Context, from: String?, contents: String?) {
        val intent = Intent(context, RecycleMainRecordActivity::class.java)
        intent.putExtra("from", from)
        intent.putExtra("contents", contents)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }
}
