package com.swu.caresheep.ui.elder.main

import android.accounts.AccountManager
import android.app.Activity
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.ElderMapsActivity
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityElderBinding
import com.swu.caresheep.elder.AlarmReceiver
import com.swu.caresheep.elder.AlarmReceiverBreakfast
import com.swu.caresheep.elder.AlarmReceiverDinner
import com.swu.caresheep.elder.AlarmReceiverLunch
import com.swu.caresheep.elder.AlarmReceiverWalk
import com.swu.caresheep.elder.ElderVoiceMainActivity
import com.swu.caresheep.elder.ElderWalkMainActivity
import com.swu.caresheep.ui.elder.connect.ElderConnectActivity
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.REQUEST_ACCOUNT_PICKER
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.REQUEST_AUTHORIZATION
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.REQUEST_GOOGLE_PLAY_SERVICES
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.timeZone
import com.swu.caresheep.ui.start.StartActivity
import com.swu.caresheep.ui.start.user_id
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import java.util.Calendar

class ElderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityElderBinding

    // Google Login
    private lateinit var client: GoogleSignInClient

    // Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    private var mService: com.google.api.services.calendar.Calendar? = null
    private var mCredential: GoogleAccountCredential? = null

    private var task: GoogleCalendarRequestTask = GoogleCalendarRequestTask(mCredential, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 설정
        window.statusBarColor = ContextCompat.getColor(this, R.color.orange_100)

        initView()

        getBreakfastAlarm()
        getDinnerAlarm()
        getLunchAlarm()
        getWalkAlarm()
        getMedicineAlarm()
    }

    override fun onStart() {
        super.onStart()

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
                // 로그아웃 이후의 이벤트들(토스트 메세지, 화면 종료)을 여기서 수행하면 됨
                val intent = Intent(this, StartActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
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
        val today = Calendar.getInstance(timeZone)

        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
            this,
            listOf(*GuardianCalendarFragment.SCOPES)
        ).setBackOff(ExponentialBackOff())  // I/O 예외 상황을 대비해서 백오프 정책 사용

        // Google Calendar API 호출
        getResultsFromApi(today)
    }


    /**
     * 다음 사전 조건을 모두 만족해야 Google Calendar API를 사용할 수 있다.
     *
     * 사전 조건
     * - Google Play Services 설치
     * - 유효한 구글 계정 선택
     * - 안드로이드 디바이스에서 인터넷 사용 가능
     *
     * 하나라도 만족하지 않으면 해당 사항을 사용자에게 알림.
     */
    private fun getResultsFromApi(selectedDate: Calendar?): String? {
        if (!isGooglePlayServicesAvailable()) {  // Google Play Services를 사용할 수 없는 경우
            acquireGooglePlayServices()
        } else if (mCredential!!.selectedAccountName == null) {  // 유효한 Google 계정이 선택되어 있지 않은 경우
            // 구글 계정 연결 후 Google Calendar API 호출
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
            mCredential!!.selectedAccount = lastSignedInAccount!!.account

            task = GoogleCalendarRequestTask(mCredential, selectedDate)
            task.execute()
        } else if (!isDeviceOnline()) {  // 인터넷을 사용할 수 없는 경우
            Toast.makeText(this, "인터넷을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // Google Calendar API 호출
            task = GoogleCalendarRequestTask(mCredential, selectedDate)
            task.execute()
        }
        return null
    }


    /**
     * 안드로이드 디바이스에 최신 버전의 Google Play Services가 설치되어 있는지 확인
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Google Play Services 업데이트로 해결가능하다면 사용자가 최신 버전으로 업데이트하도록 유도하기위해
     * 대화상자를 보여줌
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /**
     * 안드로이드 디바이스에 Google Play Services가 설치 안되어 있거나 오래된 버전인 경우 보여주는 대화상자
     */
    fun showGooglePlayServicesAvailabilityErrorDialog(
        connectionStatusCode: Int
    ) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog: Dialog = apiAvailability.getErrorDialog(
            this,
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )!!
        dialog.show()
    }

    /**
     * 구글 플레이 서비스 업데이트 다이얼로그, 구글 계정 선택 다이얼로그, 인증 다이얼로그에서 되돌아올때 호출된다.
     */
    override fun onActivityResult(
        requestCode: Int,  // onActivityResult가 호출되었을 때 요청 코드로 요청을 구분
        resultCode: Int,  // 요청에 대한 결과 코드
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(
                    this,
                    "앱을 실행시키려면 구글 플레이 서비스가 필요합니다. 구글 플레이 서비스를 설치 후 다시 실행하세요.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                getResultsFromApi(null)
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings: SharedPreferences =
                        getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(GuardianCalendarFragment.PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential!!.selectedAccountName = accountName
                    getResultsFromApi(null)
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                getResultsFromApi(null)
            }
        }
    }


    /**
     * Android 6.0 (API 23) 이상에서 런타임 권한 요청시 결과를 리턴받음
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,  //requestPermissions(android.app.Activity, String, int, String[])에서 전달된 요청 코드
        permissions: Array<String?>,  // 요청한 퍼미션
        grantResults: IntArray // 퍼미션 처리 결과. PERMISSION_GRANTED 또는 PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }

    /**
     * 안드로이드 디바이스가 인터넷 연결되어 있는지 확인한다. 연결되어 있다면 True 리턴, 아니면 False 리턴
     */
    private fun isDeviceOnline(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    /**
     * 캘린더 이름에 대응하는 캘린더 ID를 리턴
     */
    private fun getCalendarID(calendarTitle: String): String? {
        var id: String? = null

        // Iterate through entries in calendar list
        var pageToken: String? = null
        do {
            var calendarList: CalendarList? = null
            try {
                calendarList = mService!!.calendarList().list().setPageToken(pageToken).execute()
            } catch (e: UserRecoverableAuthIOException) {
                startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val items: List<CalendarListEntry>? = calendarList?.items

            if (items != null) {
                for (calendarListEntry in items) {
                    if (calendarListEntry.summary.toString() == calendarTitle) {
                        id = calendarListEntry.id.toString()
                    }
                }
                pageToken = calendarList?.nextPageToken
            }
        } while (pageToken != null)

        return id
    }

    /**
     * 비동기적으로 Google Calendar API 호출
     */
    private inner class GoogleCalendarRequestTask(
        credential: GoogleAccountCredential?,
        private var selectedDate: Calendar?,
    ) {
        private var mLastError: Exception? = null

        // 일정 데이터 리스트 선언
        var todayScheduleData = ArrayList<ElderTodaySchedule>()
        var todayScheduleRVAdapter = ElderTodayScheduleRVAdapter(todayScheduleData)

        init {
            val transport: HttpTransport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            mService = com.google.api.services.calendar.Calendar.Builder(
                transport,
                jsonFactory,
                credential
            )
                .setApplicationName("CareSheep")
                .build()
        }

        fun execute() = lifecycleScope.launch {
            onPreExecute()

            try {
                val result = withContext(Dispatchers.IO) {
                    try {
                        getEvent(selectedDate)
                    } catch (e: Exception) {
                        mLastError = e
                        onCancelled()
                        null
                    }
                }

                onPostExecute(result)
            } catch (e: Exception) {
                mLastError = e
                onCancelled()
            }
        }


        private fun onPreExecute() {
            binding.rvTodaySchedule.adapter = todayScheduleRVAdapter
            binding.llTodayScheduleNotExist.visibility = View.INVISIBLE
            binding.pbTodayScheduleLoading.show()
        }


        /**
         * CalendarTitle 이름의 캘린더에서 해당 날짜의 일정을 가져와 리턴
         */
        @Throws(IOException::class)
        private fun getEvent(selectedDate: Calendar?): List<ElderTodaySchedule>? {

            Log.e("[GetEvent 입장]selectedDate: ", selectedDate.toString())

            val testDate: Calendar =
                selectedDate ?: Calendar.getInstance(timeZone)

            // 선택된 날짜로부터 시작과 끝 시간을 계산
            val startOfDay = testDate.clone() as Calendar
            startOfDay.set(Calendar.HOUR_OF_DAY, 0)
            startOfDay.set(Calendar.MINUTE, 0)
            startOfDay.set(Calendar.SECOND, 0)

            val endOfDay = testDate.clone() as Calendar
            endOfDay.set(Calendar.HOUR_OF_DAY, 23)
            endOfDay.set(Calendar.MINUTE, 59)
            endOfDay.set(Calendar.SECOND, 59)


            val calendarID: String? = getCalendarID("공유 캘린더")
            if (calendarID == null) {
                binding.llTodayScheduleNotExist.visibility = View.VISIBLE
                binding.rvTodaySchedule.visibility = View.INVISIBLE

                return null
            }

            val events: Events = mService!!.events().list(calendarID)
                .setTimeMin(DateTime(startOfDay.timeInMillis))
                .setTimeMax(DateTime(endOfDay.timeInMillis))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()

            val items: List<Event> = events.items

            // 일정 데이터 리스트 선언
            val scheduleData = ArrayList<ElderTodaySchedule>()

            // CalendarView에 일정 표시
            val calendar = Calendar.getInstance(timeZone)
            items.forEach { event ->
                var eventTitle = event.summary
                if (eventTitle.isNullOrEmpty()) {
                    eventTitle = "(제목 없음)"
                }
                val start = event.start.dateTime
                val end = event.end.dateTime

                var startTime = ""
                val typeStartDate = event.start.date
                val typeEndDate = event.end.date

                var type = 0
                if (start != null && end != null) {
                    // 시간 지정 일정
                    type = 0
                } else if (typeStartDate != null && typeEndDate != null) {
                    // 종일 일정
                    type = 1
                }

                if (start != null) {
                    // 한국 시간대로 설정
                    val koreaTimeZone = TimeZone.getTimeZone("Asia/Seoul")
                    calendar.timeZone = koreaTimeZone

                    // 일정 시작 시간 계산
                    val startDate = Date(start.value)
                    calendar.time = startDate
                    val startHour = calendar.get(Calendar.HOUR_OF_DAY)
                    val startMinute = calendar.get(Calendar.MINUTE)
                    val startAMPM = calendar.get(Calendar.AM_PM)

                    val strStartMinute = if (startMinute / 10 == 0) "0$startMinute" else startMinute
                    val strStartAMPM = if (startAMPM == Calendar.AM) "오전"
                    else "오후"
                    val startHour12 =
                        if (startHour == 0) 12 else if (startHour > 12) startHour - 12 else startHour

                    startTime = "$strStartAMPM ${startHour12}:${strStartMinute}"
                }

                scheduleData.add(ElderTodaySchedule(startTime, type, eventTitle))
            }

            Log.e("calendar", scheduleData.size.toString() + "개의 데이터를 가져왔습니다.")

            return scheduleData
        }

        private fun onPostExecute(result: List<ElderTodaySchedule>?) {

            result?.let {
                if (result.isNotEmpty()) {
                    // RecyclerView 어댑터와 데이터 리스트 연결
                    todayScheduleRVAdapter =
                        ElderTodayScheduleRVAdapter(it as ArrayList<ElderTodaySchedule>)
                    binding.rvTodaySchedule.adapter = todayScheduleRVAdapter

                    // 일정 있으므로 RV 보이게 설정
                    binding.llTodayScheduleNotExist.visibility = View.INVISIBLE
                    binding.rvTodaySchedule.visibility = View.VISIBLE
                } else {
                    // 일정 없으므로 RV 안 보이게 설정
                    binding.llTodayScheduleNotExist.visibility = View.VISIBLE
                    binding.rvTodaySchedule.visibility = View.INVISIBLE

                    val context = applicationContext
                    val resources = context?.resources
                    val animation =
                        resources?.let { AnimationUtils.loadAnimation(context, R.anim.fade_in) }

                    animation?.also { hyperspaceJumpAnimation ->
                        binding.llTodayScheduleNotExist.startAnimation(hyperspaceJumpAnimation)
                    }
                }
            }
            binding.pbTodayScheduleLoading.hide()
        }

        private fun onCancelled() {
            binding.pbTodayScheduleLoading.hide()

            if (mLastError != null) {
                when (mLastError) {
                    is GooglePlayServicesAvailabilityIOException -> {
                        showGooglePlayServicesAvailabilityErrorDialog(
                            (mLastError as GooglePlayServicesAvailabilityIOException)
                                .connectionStatusCode
                        )
                    }
                    is UserRecoverableAuthIOException -> {
                        startActivityForResult(
                            (mLastError as UserRecoverableAuthIOException).intent,
                            REQUEST_AUTHORIZATION
                        )
                    }
                    else -> {
                        Log.e(
                            "어르신 메인: ",
                            "GoogleCalendarRequestTask The following error occurred: ${mLastError!!.message}"
                        )
                    }
                }
            } else {
                Log.e("어르신 메인: ", "요청이 취소됐습니다.")
            }
        }

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
}