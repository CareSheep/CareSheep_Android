package com.swu.caresheep.elder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.ui.elder.main.currentSteps
import com.swu.caresheep.ui.elder.main.emergency_id
import com.swu.caresheep.ui.elder.main.isWalking
import com.swu.caresheep.ui.start.user_id
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AlarmReceiverCheckEmergency : BroadcastReceiver() {
    // 매일 수면 시작 시간 + 8시간 후에 emergency 체크

    // 긴급상황 DB 연결
    private lateinit var dbRef: DatabaseReference

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "Check") {
            // 매일 자정에 실행하고자 하는 코드를 여기에 작성합니다.
            // 예를 들어, 특정 함수를 호출하거나 작업을 수행할 수 있습니다.
            // 함수 호출 예: MyFunctionClass.myFunction()
            checkWalking()
            if(isWalking == false){
                var result = 1
                // DB에 저장
                val now = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val formattedDateTime = now.format(formatter)

                val data = hashMapOf(
                    "user_id" to user_id,
                    "emergency" to result,
                    "today_date" to formattedDateTime
                )

                dbRef = FirebaseDatabase.getInstance(BuildConfig.DB_URL).getReference("Emergency")
                dbRef.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val childCount = dataSnapshot.childrenCount
                        val id = (childCount + 1).toInt()
                        emergency_id = id // 긴급상황 고유번호 정해주기 -> 다음 액티비티에서 사용

                        dbRef.child(id.toString()).setValue(data)
                            .addOnSuccessListener {
                                Log.e("긴급 상황 감지", "DB에 저장 성공")
                            }.addOnFailureListener {
                                Log.e("긴급 상황 감지", "DB에 저장 실패")
                            }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("긴급 상황 감지", "Database error: $error")
                    }
                })
            }

        }
    }

    private fun checkWalking(){
        if(currentSteps > 0){
            isWalking = true
        }
    }
}