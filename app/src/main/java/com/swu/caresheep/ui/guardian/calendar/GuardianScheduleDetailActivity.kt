package com.swu.caresheep.ui.guardian.calendar

import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.gson.Gson
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianScheduleDetailBinding
import kotlinx.coroutines.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class GuardianScheduleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianScheduleDetailBinding

    // Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    private var mService: com.google.api.services.calendar.Calendar? = null
    private var mCredential: GoogleAccountCredential? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
            overridePendingTransition(R.anim.none, R.anim.slide_out_right)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianScheduleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
            this,
            listOf(*GuardianCalendarFragment.SCOPES)
        ).setBackOff(ExponentialBackOff())  // I/O 예외 상황을 대비해서 백오프 정책 사용

        // 일정 세부 정보 가져오기
        val itemJson = intent.getStringExtra("Selected Schedule")
        val gson = Gson()
        val item = gson.fromJson(itemJson, GuardianSchedule::class.java)

        // 일정 제목
        val titleEditable = Editable.Factory.getInstance().newEditable(item.title)
        binding.etScheduleTitle.text = titleEditable

        // 일정 유형 (시간 / 종일)
        if (item.type == 0) {
            binding.btnTime.isSelected = true
            binding.btnAllDay.isSelected = false
        } else {
            binding.btnTime.isSelected = false
            binding.btnAllDay.isSelected = true
        }

        // 일정 메모
        val memoEditable = if (item.memo != null) {
            Editable.Factory.getInstance().newEditable(item.memo)
        } else {
            Editable.Factory.getInstance().newEditable("")
        }
        binding.etMemo.text = memoEditable

        // 일정 시작 시간
        val startTime = LocalDateTime.parse(item.startTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
        val endTime = LocalDateTime.parse(item.endTime.toString(), DateTimeFormatter.ISO_DATE_TIME)

        val outputFormat = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", Locale.KOREAN)
        val outputFormatWithTime = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E) a hh:mm", Locale.KOREAN)

        val formattedStartDate: String = if (item.type == 1) {
            startTime.format(outputFormat)
        } else {
            startTime.format(outputFormatWithTime)
        }

        val formattedEndDate: String = if (item.type == 1) {
            endTime.format(outputFormat)
        } else {
            endTime.format(outputFormatWithTime)
        }

        binding.tvStartTime.text = formattedStartDate
        binding.tvEndTime.text = formattedEndDate

//        // 일정 format
//        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
//        val startDate: Date = inputFormat.parse(item.startTime.toString())!!
//        val endDate: Date = inputFormat.parse(item.endTime.toString())!!
//
//        val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREAN)
//        val outputFormatWithTime = SimpleDateFormat("yyyy년 MM월 dd일 (E) a hh:mm", Locale.KOREAN)
//
//        // 일정 시작 시간
//        val formattedStartDate: String = if (item.type == 1) {
//            outputFormat.format(startDate)
//        } else {
//            outputFormatWithTime.format(startDate)
//        }
//
//        // 일정 종료 시간
//        val formattedEndDate: String = if (item.type == 1) {
//            outputFormat.format(endDate)
//        } else {
//            outputFormatWithTime.format(endDate)
//        }
//
//        binding.tvStartTime.text = formattedStartDate
//        binding.tvEndTime.text = formattedEndDate


        // 일정 알림
        binding.tvAlarm.text = item.notification

        // 일정 반복
        binding.tvRepeat.text = item.repeat

        // 삭제 버튼
        binding.btnDelete.setOnClickListener {
            deleteSchedule(item.eventId)

            val sharedPrefs = getSharedPreferences("Delete Schedule", Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean("isDeleted", true).apply()

            onBackPressedCallback.handleOnBackPressed()
            Toast.makeText(this, "일정을 삭제했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        // 뒤로 가기
        binding.ivBack.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
    }

    private fun deleteSchedule(eventId: String) {
        getResultsFromApi(eventId)
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
    private fun getResultsFromApi(eventId: String): String? {
        if (!isGooglePlayServicesAvailable()) {  // Google Play Services를 사용할 수 없는 경우
            acquireGooglePlayServices()
        } else if (mCredential!!.selectedAccountName == null) {  // 유효한 Google 계정이 선택되어 있지 않은 경우
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
            mCredential!!.selectedAccount = lastSignedInAccount!!.account

            GoogleCalendarRequestTask(
                mCredential,
                eventId
            ).execute()
        } else if (!isDeviceOnline()) {  // 인터넷을 사용할 수 없는 경우
            Toast.makeText(this, "인터넷을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // Google Calendar API 호출
            GoogleCalendarRequestTask(
                mCredential,
                eventId
            ).execute()
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
    private fun showGooglePlayServicesAvailabilityErrorDialog(
        connectionStatusCode: Int
    ) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog: Dialog = apiAvailability.getErrorDialog(
            this,
            connectionStatusCode,
            GuardianCalendarFragment.REQUEST_GOOGLE_PLAY_SERVICES
        )!!
        dialog.show()
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
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
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
                startActivityForResult(e.intent, GuardianCalendarFragment.REQUEST_AUTHORIZATION)
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
        private var eventId: String
    ) {
        private var mLastError: Exception? = null
        var job: Job? = null

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


        /**
         * 백그라운드에서 Google Calendar API 호출 처리
         */
        fun execute() {
            job = CoroutineScope(Dispatchers.Main).launch {

                try {
                    withContext(Dispatchers.IO) {
                        try {
                            deleteEvent(eventId)
                            null
                        } catch (e: Exception) {
                            mLastError = e
                            null
                        }
                    }

                } catch (e: Exception) {
                    mLastError = e
                }
            }
        }


        private fun deleteEvent(eventId: String): String {
            val calendarID: String = getCalendarID("공유 캘린더") ?: return "공유 캘린더를 먼저 생성하세요."
            try {
                mService!!.events().delete(calendarID, eventId).execute()
                Log.e("[deleteEvent]", "삭제")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Exception", "Exception : $e")
            }

            return "deleted : "
        }
    }
}