package com.swu.caresheep.ui.elder.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.ui.elder.map.ElderMapsActivity
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityElderBinding
import com.swu.caresheep.elder.AlarmReceiver
import com.swu.caresheep.elder.AlarmReceiverBreakfast
import com.swu.caresheep.elder.AlarmReceiverCheckEmergency
import com.swu.caresheep.elder.AlarmReceiverDinner
import com.swu.caresheep.elder.AlarmReceiverLunch
import com.swu.caresheep.elder.AlarmReceiverWalk
import com.swu.caresheep.elder.ElderVoiceMainActivity
import com.swu.caresheep.elder.ElderWalkMainActivity
import com.swu.caresheep.ui.elder.connect.ElderConnectActivity
import com.swu.caresheep.ui.start.StartActivity
import com.swu.caresheep.ui.start.user_id
import com.swu.caresheep.utils.CalendarUtil
import com.swu.caresheep.utils.CalendarUtil.Companion.SEOUL_TIME_ZONE
import com.swu.caresheep.utils.LocationUtil
import java.util.Calendar

var emergency_id: Int = 0

// 현재 걸음 수
var currentSteps = 0
// 어른신이 걸으시는지(긴급 상황이 아닌지) 확인
var isWalking : Boolean = false

class ElderActivity : AppCompatActivity(), SensorEventListener {


    /**긴급 상황 대비를 위한 걷기 감지 기능**/

    lateinit var sensorManager: SensorManager
    var stepCountSensor: Sensor? = null
    lateinit var stepCountView: TextView

    var sleepTimeHour :Int = 0
    var sleepTimeMinute :Int = 0
    var emergencyTimeHour :Int = 0


    // 긴급상황 DB 연결
    private lateinit var dbRef: DatabaseReference

    private lateinit var binding: ActivityElderBinding
    private lateinit var calendarUtil: CalendarUtil

    private lateinit var client: GoogleSignInClient


    @RequiresApi(api = Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 설정
        window.statusBarColor = ContextCompat.getColor(this, R.color.orange_100)

        calendarUtil = CalendarUtil(this, null, this, binding)

        // 위치 권한 요청
        LocationUtil.initForegroundLocationUtil(this)

        // 3분 = 180초 동안 걷지 않으면 걷지 않음 알림

//            // 밀리 초
//            checkWalking()
//            if(isWalking == false){
//                var result = 1
//                // DB에 저장
//                val now = LocalDateTime.now()
//                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
//                val formattedDateTime = now.format(formatter)
//
//                val data = hashMapOf(
//                    "user_id" to 1,
//                    "emergency" to result,
//                    "today_date" to formattedDateTime
//                )
//
//                dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("Emergency")
//                dbRef.addListenerForSingleValueEvent(object: ValueEventListener{
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        val childCount = dataSnapshot.childrenCount
//                        val id = (childCount + 1).toInt()
//                        emergency_id = id // 긴급상황 고유번호 정해주기 -> 다음 액티비티에서 사용
//
//                        dbRef.child(id.toString()).setValue(data)
//                            .addOnSuccessListener {
//                                Log.e("긴급 상황 감지", "DB에 저장 성공")
//                            }.addOnFailureListener {
//                                Log.e("긴급 상황 감지", "DB에 저장 실패")
//                            }
//                    }
//                    override fun onCancelled(error: DatabaseError) {
//                        Log.e("긴급 상황 감지", "Database error: $error")
//                    }
//                })
//            }
//        }
        initView()

        getBreakfastAlarm()
        getDinnerAlarm()
        getLunchAlarm()
        getSleepTime()
        getWalkAlarm()
        getMedicineAlarm()
        checkEmergency()
    }

    override fun onStart() {
        super.onStart()

        // 어르신 사용자가 걷는지 확인
        stepCountSensor?.let {
            // Set sensor speed
            sensorManager
            sensorManager.registerListener(
                this,
                stepCountSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }

        // 음성 메시지 전송 버튼
        binding.btnVoiceRecord.setOnClickListener {
            val intent = Intent(this, ElderVoiceMainActivity::class.java)
            startActivity(intent)
        }

        // 만보기 측정 버튼
        binding.btnWalk.setOnClickListener {
            val intent = Intent(this, ElderWalkMainActivity::class.java)
            startActivity(intent)
        }

        // 보호자 연결 버튼
        binding.btnConnect.setOnClickListener {
            val intent = Intent(this, ElderConnectActivity::class.java)
            startActivity(intent)
        }

        // 위치 추적 지도 버튼
        binding.btnMap.setOnClickListener {
            val intent = Intent(this, ElderMapsActivity::class.java)
            startActivity(intent)
        }

        // 새로 고침 버튼
        binding.btnUpdateTodaySchedule.setOnClickListener {
            initView()
        }

        // 로그아웃 버튼
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun initView() {
        getUserName()
        getTodaySchedule()
    }

    /**
     * 로그아웃
     */
    private fun logout() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        client = this.let { GoogleSignIn.getClient(this, gso) }

        client.signOut()
            .addOnCompleteListener(this) {
                // 로그아웃 성공시 실행
                val intent = Intent(this, StartActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                Snackbar.make(binding.root, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    /**
     * 사용자 (어르신) 이름 불러오기
     */
    private fun getUserName() {
        Firebase.database(DB_URL)
            .getReference("Users")
            .orderByChild("id")
            .equalTo(user_id.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val userName = data.child("username").getValue(String::class.java)
                            "${userName}님,".also { binding.tvElderName.text = it }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 쿼리 실행 중 오류 발생 시 처리할 내용
                }
            })
    }

    /**
     * 오늘의 일정 가져오기
     */
    private fun getTodaySchedule() {
        val today = Calendar.getInstance(SEOUL_TIME_ZONE)

        calendarUtil.setupGoogleApi()

        calendarUtil.mID = 3  // 일정 조회
        calendarUtil.getResultsFromApi(today, null, null)
    }


    /* 루틴 가져오기 */
    private fun getBreakfastAlarm() {

        val user_id = 1 // user_id로 수정

        // Firebase Realtime Database에서 데이터 가져오기
        val database =
            FirebaseDatabase.getInstance(DB_URL)
        val reference = database.getReference("UsersRoutine").orderByChild("user_id")
            .equalTo(user_id.toDouble())
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val breakfastValue =
                            data.child("breakfast").getValue(String::class.java).toString()
                        //breakfast_time.setText("$breakfastValue")


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

                                val alarmIntent =
                                    Intent(this@ElderActivity, AlarmReceiverBreakfast::class.java)
                                val alarmManager =
                                    getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                alarmIntent.action = AlarmReceiverBreakfast.ACTION_RESTART_SERVICE
                                Log.d("AlarmService", "확인")
                                val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                    this@ElderActivity,
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

    private fun getLunchAlarm() {
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(DB_URL)
                .getReference("UsersRoutine")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val lunchValue =
                                    data.child("lunch").getValue(String::class.java).toString()
                                //lunch_time.setText("$lunchValue")

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

                                        val alarmIntent = Intent(
                                            this@ElderActivity,
                                            AlarmReceiverLunch::class.java
                                        )
                                        val alarmManager =
                                            getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        alarmIntent.action =
                                            AlarmReceiverLunch.ACTION_RESTART_SERVICE
                                        val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                            this@ElderActivity,
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


    private fun getDinnerAlarm() {
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(DB_URL)
                .getReference("UsersRoutine")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val dinnerValue =
                                    data.child("dinner").getValue(String::class.java).toString()
                                //dinner_time.setText("$dinnerValue")
                                Log.d("dinnerValue", dinnerValue)
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

                                        val alarmIntent = Intent(
                                            this@ElderActivity,
                                            AlarmReceiverDinner::class.java
                                        )
                                        val alarmManager =
                                            getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        alarmIntent.action =
                                            AlarmReceiverDinner.ACTION_RESTART_SERVICE
                                        val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                            this@ElderActivity,
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

    private fun getEmergencyTime() {
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(DB_URL)
                .getReference("UsersRoutine")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val emergencyTimeValue =
                                    data.child("emergency_time").getValue(String::class.java).toString()
                                //lunch_time.setText("$lunchValue")

                                emergencyTimeHour = emergencyTimeValue.toInt()
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

    private fun checkEmergency(){
        Log.d("긴급 상황 감지!!", " ")
        // AlarmManager 설정
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiverCheckEmergency::class.java).apply {
            action = "Check" // BroadcastReceiver에서 확인할 액션을 지정합니다.
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 8 - (12 - sleepTimeHour) + emergencyTimeHour)
        calendar.set(Calendar.MINUTE, sleepTimeMinute)
        calendar.set(Calendar.SECOND, 0)

        // 매일 자정에 알람 설정
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun getSleepTime() {
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(DB_URL)
                .getReference("UsersRoutine")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val sleepValue =
                                    data.child("sleep").getValue(String::class.java).toString()
                                Log.d("sleepValue", sleepValue)
                                // :를 시간, 분 형태로 나누기 위해 split으로 분리
                                val timeParts = sleepValue.split(":")
                                if (timeParts.size == 2) {
                                    val hour = timeParts[0].toIntOrNull()
                                    val minute = timeParts[1].toIntOrNull()
                                    // 해당 시간에 알람 설정
                                    if (hour != null && minute != null) {
                                        val calendar = Calendar.getInstance()
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        sleepTimeHour = hour
                                        Log.d("sleepTimeHour", sleepTimeHour.toString())
                                        calendar.set(Calendar.MINUTE, minute)
                                        sleepTimeMinute = minute
                                        Log.d("sleepTimeMinute", sleepTimeMinute.toString())
                                        calendar.set(Calendar.SECOND, 0)

                                        // 현재 시간보다 이전이면 다음 날로 설정하기
                                        if (calendar.before(Calendar.getInstance())) {
                                            calendar.add(Calendar.DATE, 1)
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


    private fun getWalkAlarm() {
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(DB_URL)
                .getReference("UsersRoutine")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val walkValue =
                                    data.child("walk_time").getValue(String::class.java).toString()
                                Log.d("walk", walkValue)
                                //walk_time.setText("$walkValue")
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

                                        val alarmIntent = Intent(
                                            this@ElderActivity,
                                            AlarmReceiverWalk::class.java
                                        )
                                        val alarmManager =
                                            getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        alarmIntent.action =
                                            AlarmReceiverWalk.ACTION_RESTART_SERVICE
                                        val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                            this@ElderActivity,
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


    private fun getMedicineAlarm() {
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(DB_URL)
                .getReference("MedicineTime")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val medicineValue =
                                    data.child("time").getValue(String::class.java).toString()
                                Log.d("medicine time", medicineValue)
                                //medcine_time.setText("$medicineValue")
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

                                        val alarmIntent =
                                            Intent(this@ElderActivity, AlarmReceiver::class.java)
                                        val alarmManager =
                                            getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        alarmIntent.action = AlarmReceiver.ACTION_RESTART_SERVICE
                                        val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                            this@ElderActivity,
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


    /**걷기 감지**/
    private fun checkWalking() {
        if (currentSteps > 0) {
            isWalking = true
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        // 걸음 센서 이벤트 발생시
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                // 센서 이벤트가 발생할때 마다 걸음수 증가
                currentSteps++
                stepCountView.text = currentSteps.toString()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}