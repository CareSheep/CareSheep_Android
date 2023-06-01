package com.swu.caresheep.elder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.ui.start.user_id
import kotlinx.android.synthetic.main.activity_elder_get_meal_alarm.breakfast_time
import kotlinx.android.synthetic.main.activity_elder_get_meal_alarm.dinner_time
import kotlinx.android.synthetic.main.activity_elder_get_meal_alarm.lunch_time
import kotlinx.android.synthetic.main.activity_elder_get_meal_alarm.medcine_time
import kotlinx.android.synthetic.main.activity_elder_get_meal_alarm.walk_time
import java.util.Calendar

// 1. Routine 데이터 테이블에서 값들을 알람 매니저에 저장
class ElderGetMealAlarmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elder_get_meal_alarm)

        getBrakfastAlarm()
        getLunchAlarm()
        getDinnerAlarm()
        getWalkAlarm()
        getMedicineAlarm()
    }

    private fun getBrakfastAlarm(){

        val user_id = 1 // user_id로 수정

        // Firebase Realtime Database에서 데이터 가져오기
        val database =
            FirebaseDatabase.getInstance("https://caresheep-dcb96-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val reference = database.getReference("UsersRoutine").orderByChild("user_id").equalTo(user_id.toDouble())
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(data in snapshot.children){
                        val breakfastValue = data.child("breakfast").getValue(String::class.java).toString()
                        breakfast_time.setText("$breakfastValue")


                        Log.d("Firebase", "시간 값: $breakfastValue")

                        // 시간 : 분 형태로 돼있어서 split으로 분리시킨 후 int로 변환
                        val timeParts = breakfastValue.split(":")
                        if (timeParts.size == 2) {
                            val hour = timeParts[0].toIntOrNull()
                            val minute = timeParts[1].toIntOrNull()
                            // 해당 시간에 알람 설정
                            if (hour != null && minute != null) {
                                val calendar = Calendar.getInstance()
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)
                                calendar.set(Calendar.SECOND, 0)

                                // 현재 시간보다 이전이면 다음 날로 설정
                                if (calendar.before(Calendar.getInstance())) {
                                    calendar.add(Calendar.DATE, 1)
                                }

                                val alarmIntent = Intent(this@ElderGetMealAlarmActivity, AlarmReceiverBreakfast::class.java)
                                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                alarmIntent.action = AlarmReceiverBreakfast.ACTION_RESTART_SERVICE
                                Log.d("AlarmService", "확인")
                                val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                    this@ElderGetMealAlarmActivity,
                                    0,
                                    alarmIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManager.setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        calendar.timeInMillis,
                                        alarmCallPendingIntent
                                    )
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    alarmManager.setExact(
                                        AlarmManager.RTC_WAKEUP,
                                        calendar.timeInMillis,
                                        alarmCallPendingIntent
                                    )
                                }

                                // 알람 설정
                                //alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmCallPendingIntent)


//                                alarmManager.setExactAndAllowWhileIdle(
//                                    AlarmManager.RTC_WAKEUP,
//                                    calendar.timeInMillis,
//                                    alarmCallPendingIntent
//                                )
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 가져오기가 실패한 경우 처리할 내용
                val exception = databaseError.toException()
                Log.e("FirebaseDatabase", "Failed to read value: ${exception.message}", exception)

            }
        })
    }

    private fun getLunchAlarm(){
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("UsersRoutine")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val lunchValue = data.child("lunch").getValue(String::class.java).toString()
                                lunch_time.setText("$lunchValue")

                                // :를 시간, 분 형태로 나누기 위해 split으로 분리
                                val timeParts = lunchValue.split(":")
                                if (timeParts.size == 2) {
                                    val hour = timeParts[0].toIntOrNull()
                                    val minute = timeParts[1].toIntOrNull()
                                    // 해당 시간에 알람 설정
                                    if (hour != null && minute != null) {
                                        val calendar = Calendar.getInstance()
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        calendar.set(Calendar.MINUTE, minute)
                                        calendar.set(Calendar.SECOND, 0)

                                        // 현재 시간보다 이전이면 다음 날로 설정하기
                                        if (calendar.before(Calendar.getInstance())) {
                                            calendar.add(Calendar.DATE, 1)
                                        }

                                        val alarmIntent = Intent(this@ElderGetMealAlarmActivity, AlarmReceiverLunch::class.java)
                                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        alarmIntent.action = AlarmReceiverLunch.ACTION_RESTART_SERVICE
                                        val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                            this@ElderGetMealAlarmActivity,
                                            0,
                                            alarmIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                        )

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            alarmManager.setExactAndAllowWhileIdle(
                                                AlarmManager.RTC_WAKEUP,
                                                calendar.timeInMillis,
                                                alarmCallPendingIntent
                                            )
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            alarmManager.setExact(
                                                AlarmManager.RTC_WAKEUP,
                                                calendar.timeInMillis,
                                                alarmCallPendingIntent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }
    }


    private fun getDinnerAlarm(){
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("UsersRoutine")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dinnerValue = data.child("dinner").getValue(String::class.java).toString()
                                dinner_time.setText("$dinnerValue")
                                Log.d("dinnerValue","$dinnerValue")
                                // :를 시간, 분 형태로 나누기 위해 split으로 분리
                                val timeParts = dinnerValue.split(":")
                                if (timeParts.size == 2) {
                                    val hour = timeParts[0].toIntOrNull()
                                    val minute = timeParts[1].toIntOrNull()
                                    // 해당 시간에 알람 설정
                                    if (hour != null && minute != null) {
                                        val calendar = Calendar.getInstance()
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        calendar.set(Calendar.MINUTE, minute)
                                        calendar.set(Calendar.SECOND, 0)

                                        // 현재 시간보다 이전이면 다음 날로 설정하기
                                        if (calendar.before(Calendar.getInstance())) {
                                            calendar.add(Calendar.DATE, 1)
                                        }

                                        val alarmIntent = Intent(this@ElderGetMealAlarmActivity, AlarmReceiverDinner::class.java)
                                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        alarmIntent.action = AlarmReceiverDinner.ACTION_RESTART_SERVICE
                                        val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                            this@ElderGetMealAlarmActivity,
                                            0,
                                            alarmIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                        )

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            alarmManager.setExactAndAllowWhileIdle(
                                                AlarmManager.RTC_WAKEUP,
                                                calendar.timeInMillis,
                                                alarmCallPendingIntent
                                            )
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            alarmManager.setExact(
                                                AlarmManager.RTC_WAKEUP,
                                                calendar.timeInMillis,
                                                alarmCallPendingIntent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }
    }


    private fun getWalkAlarm(){
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("UsersRoutine")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val walkValue = data.child("walk_time").getValue(String::class.java).toString()
                                Log.d("walk","$walkValue")
                                walk_time.setText("$walkValue")
                                // :를 시간, 분 형태로 나누기 위해 split으로 분리
                                val timeParts = walkValue.split(":")
                                if (timeParts.size == 2) {
                                    val hour = timeParts[0].toIntOrNull()
                                    val minute = timeParts[1].toIntOrNull()
                                    // 해당 시간에 알람 설정
                                    if (hour != null && minute != null) {
                                        val calendar = Calendar.getInstance()
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        calendar.set(Calendar.MINUTE, minute)
                                        calendar.set(Calendar.SECOND, 0)

                                        // 현재 시간보다 이전이면 다음 날로 설정하기
                                        if (calendar.before(Calendar.getInstance())) {
                                            calendar.add(Calendar.DATE, 1)
                                        }

                                        val alarmIntent = Intent(this@ElderGetMealAlarmActivity, AlarmReceiverWalk::class.java)
                                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        alarmIntent.action = AlarmReceiverWalk.ACTION_RESTART_SERVICE
                                        val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                            this@ElderGetMealAlarmActivity,
                                            0,
                                            alarmIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                        )

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            alarmManager.setExactAndAllowWhileIdle(
                                                AlarmManager.RTC_WAKEUP,
                                                calendar.timeInMillis,
                                                alarmCallPendingIntent
                                            )
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            alarmManager.setExact(
                                                AlarmManager.RTC_WAKEUP,
                                                calendar.timeInMillis,
                                                alarmCallPendingIntent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }
    }


    private fun getMedicineAlarm(){
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("MedicineTime")
                .orderByChild("user_id")
                .equalTo(user_id.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val medicineValue = data.child("time").getValue(String::class.java).toString()
                                Log.d("medicine time","$medicineValue")
                                medcine_time.setText("$medicineValue")
                                // :를 시간, 분 형태로 나누기 위해 split으로 분리
                                val timeParts = medicineValue.split(":")
                                if (timeParts.size == 2) {
                                    val hour = timeParts[0].toIntOrNull()
                                    val minute = timeParts[1].toIntOrNull()
                                    // 해당 시간에 알람 설정
                                    if (hour != null && minute != null) {
                                        val calendar = Calendar.getInstance()
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        calendar.set(Calendar.MINUTE, minute)
                                        calendar.set(Calendar.SECOND, 0)

                                        // 현재 시간보다 이전이면 다음 날로 설정하기
                                        if (calendar.before(Calendar.getInstance())) {
                                            calendar.add(Calendar.DATE, 1)
                                        }

                                        val alarmIntent = Intent(this@ElderGetMealAlarmActivity, AlarmReceiver::class.java)
                                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        alarmIntent.action = AlarmReceiver.ACTION_RESTART_SERVICE
                                        val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                            this@ElderGetMealAlarmActivity,
                                            0,
                                            alarmIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                        )

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            alarmManager.setExactAndAllowWhileIdle(
                                                AlarmManager.RTC_WAKEUP,
                                                calendar.timeInMillis,
                                                alarmCallPendingIntent
                                            )
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            alarmManager.setExact(
                                                AlarmManager.RTC_WAKEUP,
                                                calendar.timeInMillis,
                                                alarmCallPendingIntent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }
    }


}