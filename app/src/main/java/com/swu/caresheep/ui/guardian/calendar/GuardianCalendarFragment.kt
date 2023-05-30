package com.swu.caresheep.ui.guardian.calendar

import android.accounts.AccountManager
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.*
import com.google.api.services.calendar.model.Calendar
import com.google.gson.Gson
import com.swu.caresheep.R
import com.swu.caresheep.databinding.FragmentGuardianCalendarBinding
import com.swu.caresheep.utils.GoogleLoginClient
import kotlinx.coroutines.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class GuardianCalendarFragment : Fragment() {

    private lateinit var binding: FragmentGuardianCalendarBinding

    // Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    private var mService: com.google.api.services.calendar.Calendar? = null
    private var mCredential: GoogleAccountCredential? = null

    // Google Calendar API 호출 관련 메커니즘 및 AsyncTask을 재사용하기 위해 사용
    private var mID = 0
    private var googleLoginClient: GoogleLoginClient = GoogleLoginClient()

    private var selectedCalendar = java.util.Calendar.getInstance(timeZone)

    companion object {
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002

        const val PREF_ACCOUNT_NAME = "accountName"
        val SCOPES = arrayOf(CalendarScopes.CALENDAR)
        val timeZone = TimeZone.getTimeZone("Asia/Seoul")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuardianCalendarBinding.inflate(inflater, container, false)

        // 오늘 날짜 표시
        selectedCalendar = java.util.Calendar.getInstance(timeZone)

        val dayOfMonth = selectedCalendar.get(java.util.Calendar.DAY_OF_MONTH)
        val dayOfWeek = selectedCalendar.getDisplayName(
            java.util.Calendar.DAY_OF_WEEK,
            java.util.Calendar.SHORT,
            Locale.getDefault()
        )
        "${dayOfMonth}일 ($dayOfWeek)".also { binding.tvTodayDate.text = it }

        // 달력에 선택된 날짜를 일정 추가 화면에 전달
        var selectedDate = selectedCalendar.time

        var sharedPreferences =
            requireActivity().getSharedPreferences("SelectedDate", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putLong("selectedDate", selectedDate.time)
        editor.apply()


        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(*SCOPES)
        ).setBackOff(ExponentialBackOff())  // I/O 예외 상황을 대비해서 백오프 정책 사용

        mID = 1  // 캘린더 생성
        getResultsFromApi(selectedCalendar)

        // 선택된 날짜 반영
        binding.cvShared.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedCalendar = java.util.Calendar.getInstance(timeZone).apply {
                set(year, month, dayOfMonth)
            }
            val dayOfMonth = selectedCalendar.get(java.util.Calendar.DAY_OF_MONTH)
            val dayOfWeek = selectedCalendar.getDisplayName(
                java.util.Calendar.DAY_OF_WEEK,
                java.util.Calendar.SHORT,
                Locale.getDefault()
            )
            "${dayOfMonth}일 ($dayOfWeek)".also { binding.tvTodayDate.text = it }

            mID = 3  // 이벤트 불러오기
            getResultsFromApi(selectedCalendar)

            // 달력에 선택된 날짜를 일정 추가 화면에 전달
            selectedDate = selectedCalendar.time
            sharedPreferences =
                requireActivity().getSharedPreferences("SelectedDate", Context.MODE_PRIVATE)
            editor = sharedPreferences.edit()
            editor.putLong("selectedDate", selectedDate.time)
            editor.apply()
        }

        // 일정 추가 버튼 클릭 시
        binding.fabAddSchedule.setOnClickListener {
            val intent = Intent(requireContext(), GuardianAddScheduleActivity::class.java)
            startActivity(intent)
        }


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // 일정이 추가되거나 삭제되었으면 갱신
        val sharedPrefsDelete =
            requireActivity().getSharedPreferences("Delete Schedule", Context.MODE_PRIVATE)
        val isDeleted = sharedPrefsDelete.getBoolean("isDeleted", false)

        val sharedPrefsAdd =
            requireActivity().getSharedPreferences("Add Schedule", Context.MODE_PRIVATE)
        val isAdded = sharedPrefsAdd.getBoolean("isAdded", false)

        if (isDeleted || isAdded) {
            val handler = Handler(Looper.getMainLooper())

            handler.postDelayed({
                getResultsFromApi(selectedCalendar)

                // isDeleted, isAdded 값 초기화
                sharedPrefsDelete.edit().putBoolean("isDeleted", false).apply()
                sharedPrefsAdd.edit().putBoolean("isAdded", false).apply()
            }, 2000) // 2초 (2000 milliseconds) 후에 실행
        }

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
    private fun getResultsFromApi(selectedDate: java.util.Calendar?) {
        if (!isGooglePlayServicesAvailable()) {  // Google Play Services를 사용할 수 없는 경우
            acquireGooglePlayServices()
        } else if (mCredential!!.selectedAccountName == null) {  // 유효한 Google 계정이 선택되어 있지 않은 경우
            // 구글 계정 연결 후 Google Calendar API 호출
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
            mCredential!!.selectedAccount = lastSignedInAccount!!.account

            lifecycleScope.launch {
                val elderInfo =
                    withContext(Dispatchers.IO) { googleLoginClient.getElderInfo(requireActivity()) }
                val elderGmail = elderInfo.gmail

                GoogleCalendarRequestTask(
                    mCredential,
                    selectedDate,
                    elderGmail
                ).execute()
            }
//            Toast.makeText(requireContext(), "계정을 선택해야 합니다.", Toast.LENGTH_SHORT).show()
        } else if (!isDeviceOnline()) {
            Toast.makeText(requireContext(), "인터넷을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // Google Calendar API 호출
            lifecycleScope.launch {
                val elderInfo =
                    withContext(Dispatchers.IO) { googleLoginClient.getElderInfo(requireActivity()) }
                val elderGmail = elderInfo.gmail

                GoogleCalendarRequestTask(
                    mCredential,
                    selectedDate,
                    elderGmail
                ).execute()
            }
        }
    }


    /**
     * 안드로이드 디바이스에 최신 버전의 Google Play Services가 설치되어 있는지 확인
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireContext())
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Google Play Services 업데이트로 해결가능하다면 사용자가 최신 버전으로 업데이트하도록 유도하기위해
     * 대화상자를 보여줌
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireContext())
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
            requireActivity(),
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
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != RESULT_OK) {
                Toast.makeText(
                    requireContext(),
                    "앱을 실행시키려면 구글 플레이 서비스가 필요합니다. 구글 플레이 서비스를 설치 후 다시 실행하세요.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                getResultsFromApi(null)
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == RESULT_OK && data != null && data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings: SharedPreferences =
                        requireActivity().getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential!!.selectedAccountName = accountName
                    getResultsFromApi(null)
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == RESULT_OK) {
                getResultsFromApi(null)
            }
        }
    }


    /**
     * Android 6.0 (API 23) 이상에서 런타임 권한 요청시 결과를 리턴받음
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,  // requestPermissions(android.app.Activity, String, int, String[])에서 전달된 요청 코드
        permissions: Array<String?>,  // 요청한 퍼미션
        grantResults: IntArray  // 퍼미션 처리 결과. PERMISSION_GRANTED 또는 PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            requireContext()
        )
    }

    /**
     * 안드로이드 디바이스가 인터넷 연결되어 있는지 확인한다. 연결되어 있다면 True 리턴, 아니면 False 리턴
     */
    private fun isDeviceOnline(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        private var selectedDate: java.util.Calendar?,
        private var elderEmail: String?
    ) {
        private var mLastError: Exception? = null

        // 일정 데이터 리스트 선언
        var scheduleData = ArrayList<GuardianSchedule>()
        var scheduleRVAdapter = GuardianScheduleRVAdapter(scheduleData)

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
                val result = when (mID) {
                    1 -> withContext(Dispatchers.IO) {
                        createCalendar(selectedDate, elderEmail)
                    }
                    3 -> withContext(Dispatchers.IO) {
                        getEvent(selectedDate)
                    }
                    else -> null
                }

                onPostExecute(result)
            } catch (e: Exception) {
                mLastError = e
                onCancelled()
            }
        }


        private fun onPreExecute() {
            binding.rvSchedule.adapter = scheduleRVAdapter

            binding.llScheduleNotExist.visibility = View.INVISIBLE
            binding.rvSchedule.visibility = View.GONE

            binding.pbScheduleLoading.show()
        }


        /**
         * CalendarTitle 이름의 캘린더에서 해당 날짜의 일정을 가져와 리턴
         */
        @Throws(IOException::class)
        private fun getEvent(selectedDate: java.util.Calendar?): List<GuardianSchedule>? {

            val testDate: java.util.Calendar =
                selectedDate ?: java.util.Calendar.getInstance(timeZone)

            // 선택된 날짜로부터 시작과 끝 시간을 계산
            val startOfDay = testDate.clone() as java.util.Calendar
            startOfDay.set(java.util.Calendar.HOUR_OF_DAY, 0)
            startOfDay.set(java.util.Calendar.MINUTE, 0)
            startOfDay.set(java.util.Calendar.SECOND, 0)

            val endOfDay = testDate.clone() as java.util.Calendar
            endOfDay.set(java.util.Calendar.HOUR_OF_DAY, 23)
            endOfDay.set(java.util.Calendar.MINUTE, 59)
            endOfDay.set(java.util.Calendar.SECOND, 59)


            val calendarID: String? = getCalendarID("공유 캘린더")
            if (calendarID == null) {
                createCalendar(selectedDate, elderEmail)
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
            val scheduleData = ArrayList<GuardianSchedule>()

            // CalendarView에 일정 표시
            val calendar = java.util.Calendar.getInstance(timeZone)
            items.forEach { event ->
                val eventId = event.id
                var eventTitle = event.summary
                if (eventTitle.isNullOrEmpty()) {
                    eventTitle = "(제목 없음)"
                }
                val start = event.start.dateTime
                val end = event.end.dateTime
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

                // 시작, 종료 시간
                val startDate = if (start != null) {
                    calendar.timeInMillis = start.value
                    DateTime(calendar.timeInMillis)
                } else {
                    DateTime(startOfDay.timeInMillis)
                }

                val endDate = if (end != null) {
                    calendar.timeInMillis = end.value
                    DateTime(calendar.timeInMillis)
                } else {
                    DateTime(endOfDay.timeInMillis)
                }

                // 메모 정보 가져오기
                val memo: String? = event.description

                // 알림 정보 가져오기
                val notificationList: List<EventReminder> =
                    event.reminders?.overrides ?: emptyList()
                var notification = "알림 없음"
                for (item in notificationList) {
                    notification = when (item.minutes) {
                        0 -> "일정 시작시간"
                        10 -> "10분 전"
                        60 -> "1시간 전"
                        else -> "1일 전"
                    }
                }

                // 반복 정보 가져오기
                val repeatList: List<String> = event.recurrence ?: emptyList()
//                Log.e("repeatList", event.summary + " : " + repeatList.toString())

                var repeat = "반복 안 함"
                for (item in repeatList) {
                    repeat = when (item) {
                        "RRULE:FREQ=DAILY" -> "매일"
                        "RRULE:FREQ=WEEKLY" -> "매주"
                        "RRULE:FREQ=MONTHLY" -> "매월"
                        "RRULE:FREQ=YEARLY" -> "매년"
                        else -> "반복 안 함"
                    }
                }


                scheduleData.add(
                    GuardianSchedule(
                        eventId,
                        type,
                        startDate,
                        endDate,
                        eventTitle,
                        notification,
                        repeat,
                        memo
                    )
                )

//                Log.e("event", event.toString())
            }



            Log.e("[보호자] 공유 캘린더", scheduleData.size.toString() + "개의 데이터를 가져왔습니다.")

            return scheduleData
        }

        /**
         * 선택되어 있는 Google 계정에 새 캘린더를 추가
         */
        @Throws(IOException::class)
        private fun createCalendar(
            selectedDate: java.util.Calendar?,
            elderEmail: String?
        ): List<GuardianSchedule>? {
            val ids: String? = getCalendarID("공유 캘린더")
            if (ids != null) {
                Log.e("[보호자] 공유 캘린더", "이미 캘린더가 생성되어 있습니다.")
                return getEvent(selectedDate)
            }

            // 새로운 캘린더 생성
            val calendar = Calendar()

            // 캘린더의 제목 설정
            calendar.summary = "공유 캘린더"

            // 캘린더의 시간대 설정
            calendar.timeZone = "Asia/Seoul"

            // 구글 캘린더에 새로 만든 캘린더를 추가
            val createdCalendar: Calendar = mService!!.calendars().insert(calendar).execute()

            // 추가한 캘린더의 ID를 가져옴
            val calendarId: String = createdCalendar.id

            // 캘린더를 공유할 사용자 이메일 주소 지정
            val userEmail = elderEmail  // 연결된 어르신 계정

            // 공유할 사용자에 대한 권한 설정
            val rule = AclRule().apply {
                scope = AclRule.Scope().apply {
                    type = "user"
                    value = userEmail
                }
                role = "reader"  // 읽기 권한
            }

            // 캘린더에 권한 추가
            mService!!.acl().insert(calendarId, rule).execute()

            // 구글 캘린더의 캘린더 목록에서 새로 만든 캘린더를 검색
            val calendarListEntry: CalendarListEntry =
                mService!!.calendarList().get(calendarId).execute()

            // 캘린더의 배경색을 파란색으로 표시  RGB
            calendarListEntry.backgroundColor = "#ABC270"

            // 변경한 내용을 구글 캘린더에 반영
            mService!!.calendarList()
                .update(calendarListEntry.id, calendarListEntry)
                .setColorRgbFormat(true)
                .execute()

            // 새로 추가한 캘린더의 ID를 리턴
            return null
        }

        private fun onPostExecute(result: List<GuardianSchedule>?) {

            result?.let {
                if (result.isNotEmpty()) {
                    // RecyclerView 어댑터와 데이터 리스트 연결
                    scheduleRVAdapter = GuardianScheduleRVAdapter(it as ArrayList<GuardianSchedule>)
                    binding.rvSchedule.adapter = scheduleRVAdapter

                    // 일정 있으므로 RV 보이게 설정
                    binding.llScheduleNotExist.visibility = View.INVISIBLE
                    binding.rvSchedule.visibility = View.VISIBLE

                    scheduleRVAdapter.setMyItemClickListener(object :
                        GuardianScheduleRVAdapter.MyItemClickListener {
                        override fun onItemClick(schedule: GuardianSchedule) {
                            // Item 클릭 시 일정 세부 페이지로 이동
                            val gson = Gson()
                            val itemJson = gson.toJson(schedule)

                            val intent = Intent(
                                requireContext(),
                                GuardianScheduleDetailActivity::class.java
                            )
                            intent.putExtra("Selected Schedule", itemJson)
                            startActivity(intent)
                        }
                    })
                } else {
                    // 일정 없으므로 RV 안 보이게 설정
                    binding.llScheduleNotExist.visibility = View.VISIBLE
                    binding.rvSchedule.visibility = View.GONE

                    val context = activity?.applicationContext
                    val resources = context?.resources
                    val animation =
                        resources?.let { AnimationUtils.loadAnimation(context, R.anim.fade_in) }

                    animation?.also { hyperspaceJumpAnimation ->
                        binding.llScheduleNotExist.startAnimation(hyperspaceJumpAnimation)
                    }
                }
            }
            binding.pbScheduleLoading.hide()
        }

        private fun onCancelled() {
            binding.pbScheduleLoading.hide()

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
                    }
                }
            } else {
                Log.e("보호자 공유 캘린더: ", "요청이 취소됐습니다.")
            }
        }

    }

}