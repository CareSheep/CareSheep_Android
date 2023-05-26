package com.swu.caresheep.ui.guardian.calendar

import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.model.*
import com.swu.caresheep.utils.GoogleLoginClient
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianAddScheduleBinding
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.REQUEST_AUTHORIZATION
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.timeZone
import kotlinx.coroutines.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*

class GuardianAddScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianAddScheduleBinding

    // Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    private var mService: com.google.api.services.calendar.Calendar? = null
    private var mCredential: GoogleAccountCredential? = null
    private var isStartPickerClicked = false
    private var isEndPickerClicked = false
    private var scheduleTitle: String = ""
    private var scheduleMemo: String = ""

    private var googleLoginClient: GoogleLoginClient = GoogleLoginClient()

    private var task: MakeRequestTask = MakeRequestTask(mCredential, null, null)

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로가기 클릭 시 실행시킬 코드 입력
            finish()
            overridePendingTransition(R.anim.none, R.anim.slide_out_right)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
            this,
            listOf(*GuardianCalendarFragment.SCOPES)
        ).setBackOff(ExponentialBackOff())  // I/O 예외 상황을 대비해서 백오프 정책 사용

        // GuardianCalendarFragment에서 선택된 날짜 가져오기
        val sharedPreferences = getSharedPreferences("SelectedDate", MODE_PRIVATE)
        val selectedDateInMillis = sharedPreferences.getLong("selectedDate", 0)
        val selectedDate = if (selectedDateInMillis != 0L) Date(selectedDateInMillis) else Date()

        // 선택된 날짜로 초기 설정
        val calendar = java.util.Calendar.getInstance(timeZone)
        calendar.time = selectedDate

        var currentStartYear = calendar.get(java.util.Calendar.YEAR)
        var currentStartMonth = calendar.get(java.util.Calendar.MONTH) + 1
        var currentStartDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val currentStartWeek = calendar.getDisplayName(
            java.util.Calendar.DAY_OF_WEEK,
            java.util.Calendar.SHORT,
            Locale.getDefault()
        )!!

        var currentEndYear = calendar.get(java.util.Calendar.YEAR)
        var currentEndMonth = calendar.get(java.util.Calendar.MONTH) + 1
        var currentEndDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val currentEndWeek = calendar.getDisplayName(
            java.util.Calendar.DAY_OF_WEEK,
            java.util.Calendar.SHORT,
            Locale.getDefault()
        )!!

        // TimePicker 초기 설정
        binding.tpStart.hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)  // 시작 Time
        binding.tpStart.minute = calendar.get(java.util.Calendar.MINUTE)
        binding.tpEnd.hour = calendar.get(java.util.Calendar.HOUR_OF_DAY) + 1  // 종료 Time
        binding.tpEnd.minute = calendar.get(java.util.Calendar.MINUTE)


        val strCurrentMinute =
            if (calendar.get(java.util.Calendar.MINUTE) / 10 == 0) "0${calendar.get(java.util.Calendar.MINUTE)}" else calendar.get(
                java.util.Calendar.MINUTE
            )
        val strCurrentAMPM =
            if (calendar.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "오전"
            else "오후"
        val strCurrentHour12 =
            if (calendar.get(java.util.Calendar.HOUR_OF_DAY) == 0) 12 else if (calendar.get(java.util.Calendar.HOUR_OF_DAY) > 12) calendar.get(
                java.util.Calendar.HOUR_OF_DAY
            ) - 12 else calendar.get(java.util.Calendar.HOUR_OF_DAY)

        val strCurrentEndAMPM =
            if (calendar.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "오전"
            else "오후"

        val strCurrentEndHour12 =
            if (calendar.get(java.util.Calendar.HOUR_OF_DAY) + 1 > 12) calendar.get(
                java.util.Calendar.HOUR_OF_DAY
            ) + 1 - 12 else calendar.get(java.util.Calendar.HOUR_OF_DAY) + 1

        var currentTime = "$strCurrentAMPM $strCurrentHour12:$strCurrentMinute"
        var currentEndTime = "$strCurrentEndAMPM $strCurrentEndHour12:$strCurrentMinute"

        // 시작 TimePicker 시간 변경 시
        binding.tpStart.setOnTimeChangedListener { _, newHour, newMinute ->
            val strNewAmPm: String = if (newHour >= 12) "오후" else "오전"

            val strNewHour =
                if (newHour == 0) 12 else if (newHour > 12) newHour - 12 else newHour

            val strNewMinute =
                if (calendar.get(java.util.Calendar.MINUTE) / 10 == 0) "0${newMinute}" else newMinute.toString()

            currentTime = "$strNewAmPm $strNewHour:$strNewMinute"
            updateStartTimeText(
                currentStartYear,
                currentStartMonth,
                currentStartDay,
                currentStartWeek,
                currentTime
            )
        }

        // 종료 TimePicker 시간 변경 시
        binding.tpEnd.setOnTimeChangedListener { _, newHour, newMinute ->
            val strNewAmPm: String = if (newHour >= 12) "오후" else "오전"

            val strNewHour =
                if (newHour == 0) 12 else if (newHour > 12) newHour - 12 else newHour

            val strNewMinute =
                if (calendar.get(java.util.Calendar.MINUTE) / 10 == 0) "0${newMinute}" else newMinute.toString()

            currentEndTime = "$strNewAmPm $strNewHour:$strNewMinute"
            updateEndTimeText(
                currentEndYear,
                currentEndMonth,
                currentEndDay,
                currentEndWeek,
                currentEndTime
            )
        }

        // 월 초기 설정
        val months = arrayOf(
            "1월", "2월", "3월", "4월", "5월", "6월",
            "7월", "8월", "9월", "10월", "11월", "12월"
        )
        binding.npStartMonth.displayedValues = months  // 시작 Date
        binding.npStartMonth.minValue = 1
        binding.npStartMonth.maxValue = months.size

        binding.npEndMonth.displayedValues = months  // 종료 Date
        binding.npEndMonth.minValue = 1
        binding.npEndMonth.maxValue = months.size

        // 일 초기 설정
        val maxDay = getMaxDayOfMonth(currentStartYear, currentStartMonth)
        updateDayPicker(maxDay)

        // 시작 월 변경
        binding.npStartMonth.setOnValueChangedListener { _, _, newMonth ->
            if (newMonth == 1 && currentStartMonth == 12) { // 12월에서 1월로 변경할 경우
                currentStartYear++  // 연도 증가
            } else if (newMonth == 12 && currentStartMonth == 1) { // 1월에서 12월로 변경할 경우
                currentStartYear--  // 연도 감소
            }
            // 현재 월 갱신
            currentStartMonth = newMonth

            // 일 설정
            updateDayPicker(getMaxDayOfMonth(currentStartYear, currentStartMonth))

            // 시작 시간 업데이트
            updateStartTimeText(
                currentStartYear,
                currentStartMonth,
                currentStartDay,
                currentStartWeek,
                currentTime
            )
        }

        // 시작 일 변경
        binding.npStartDay.setOnValueChangedListener { _, _, newDay ->
            currentStartDay = newDay

            // 시작 시간 업데이트
            updateStartTimeText(
                currentStartYear,
                currentStartMonth,
                currentStartDay,
                currentStartWeek,
                currentTime
            )
        }

        // 종료 월 변경
        binding.npEndMonth.setOnValueChangedListener { _, _, newMonth ->
            if (newMonth == 1 && currentEndMonth == 12) { // 12월에서 1월로 변경할 경우
                currentEndYear++  // 연도 증가
            } else if (newMonth == 12 && currentEndMonth == 1) { // 1월에서 12월로 변경할 경우
                currentEndYear--  // 연도 감소
            }
            // 현재 월 갱신
            currentEndMonth = newMonth

            // 일 설정
            updateDayPicker(getMaxDayOfMonth(currentEndYear, currentEndMonth))

            // 종료 시간 업데이트
            updateEndTimeText(
                currentEndYear,
                currentEndMonth,
                currentEndDay,
                currentEndWeek,
                currentEndTime
            )
        }

        // 종료 일 변경
        binding.npEndDay.setOnValueChangedListener { _, _, newDay ->
            currentEndDay = newDay

            // 종료 시간 업데이트
            updateEndTimeText(
                currentEndYear,
                currentEndMonth,
                currentEndDay,
                currentEndWeek,
                currentEndTime
            )
        }


        // 현재 날짜로 설정
        binding.npStartMonth.value = currentStartMonth
        binding.npStartDay.value = currentStartDay
        updateStartTimeText(
            currentStartYear,
            currentStartMonth,
            currentStartDay,
            currentStartWeek,
            currentTime
        )

        binding.npEndMonth.value = currentStartMonth
        binding.npEndDay.value = currentStartDay
        updateEndTimeText(
            currentStartYear,
            currentStartMonth,
            currentStartDay,
            currentStartWeek,
            currentEndTime
        )


        // 시간 선택 시
        binding.btnTime.setOnClickListener {
            binding.btnTime.isSelected = true
            binding.btnAllDay.isSelected = false

            binding.tvStartTime.visibility = View.VISIBLE
            binding.tvEndTime.visibility = View.VISIBLE
            binding.tvStartAllDay.visibility = View.GONE
            binding.tvEndAllDay.visibility = View.GONE

            if (isStartPickerClicked) {
                binding.npStartDay.visibility = View.VISIBLE
                binding.npStartMonth.visibility = View.VISIBLE
                binding.tpStart.visibility = View.VISIBLE
            }

            if (isEndPickerClicked) {
                binding.npEndDay.visibility = View.VISIBLE
                binding.npEndMonth.visibility = View.VISIBLE
                binding.tpEnd.visibility = View.VISIBLE
            }

        }

        // 종일 선택 시
        binding.btnAllDay.setOnClickListener {
            binding.btnTime.isSelected = false
            binding.btnAllDay.isSelected = true

            binding.npStartDay.visibility = View.GONE
            binding.npStartMonth.visibility = View.GONE
            binding.tpStart.visibility = View.GONE
            binding.npEndDay.visibility = View.GONE
            binding.npEndMonth.visibility = View.GONE
            binding.tpEnd.visibility = View.GONE

            updateStartTimeText(
                currentStartYear,
                currentStartMonth,
                currentStartDay,
                currentStartWeek,
                null
            )
            updateEndTimeText(
                currentStartYear,
                currentStartMonth,
                currentStartDay,
                currentStartWeek,
                null
            )

            binding.tvStartTime.visibility = View.GONE
            binding.tvEndTime.visibility = View.GONE
            binding.tvStartAllDay.visibility = View.VISIBLE
            binding.tvEndAllDay.visibility = View.VISIBLE
        }

        initBtn()


        // 저장 버튼 클릭 시
        binding.btnSave.setOnClickListener {
            // 시작 시간 가져오기
            val startCalendar = java.util.Calendar.getInstance().apply {
                set(
                    currentStartYear,
                    currentStartMonth - 1,
                    currentStartDay,
                    binding.tpStart.hour,
                    binding.tpStart.minute
                )
                timeZone = GuardianCalendarFragment.timeZone
            }
            val startDateTime = DateTime(startCalendar.time)


            // 종료 시간 가져오기
            val endCalendar = java.util.Calendar.getInstance().apply {
                set(
                    currentEndYear,
                    currentEndMonth - 1,
                    currentEndDay,
                    binding.tpEnd.hour,
                    binding.tpEnd.minute
                )
                timeZone = GuardianCalendarFragment.timeZone
            }
            val endDateTime = DateTime(endCalendar.time)


            // 일정 추가
            lifecycleScope.launch {
                val elderInfo =
                    withContext(Dispatchers.IO) { googleLoginClient.getElderInfo(this@GuardianAddScheduleActivity) }
                val gmail = elderInfo.gmail
                val eventInfo =
                    getNewEventInfo(scheduleTitle, scheduleMemo, startDateTime, endDateTime)

                addSchedule(eventInfo, listOf(gmail!!))
            }

            onBackPressedCallback.handleOnBackPressed()
            Toast.makeText(this, "일정을 추가했습니다.", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStop() {
        super.onStop()
        task.job?.cancel()
    }

    /**
     * 새 일정 정보 가져오기
     */
    private fun getNewEventInfo(
        title: String,
        memo: String,
        startDateTime: DateTime,
        endDateTime: DateTime
    ): Event {
        val newEvent: Event = Event()
            .setSummary(title)
            .setDescription(memo)

        // 시작 시간
        val start = EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone("Asia/Seoul")
        newEvent.start = start


        // 종료 시간
        val end = EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone("Asia/Seoul")
        newEvent.end = end

        return newEvent
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        // 뒤로 가기
        binding.ivBack.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
        // 취소 버튼 클릭 시
        binding.btnCancel.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
    }


    /**
     * 버튼 초기 설정
     */
    private fun initBtn() {
        // 시간 및 종일 선택 초기 설정
        binding.btnTime.isSelected = true
        binding.btnAllDay.isSelected = false

        // 날짜 및 시간 선택 visibility 설정
        binding.clStart.setOnClickListener {
            isStartPickerClicked = !isStartPickerClicked
            if (isStartPickerClicked) {
                isEndPickerClicked = false
                binding.npStartDay.visibility = View.VISIBLE
                binding.npStartMonth.visibility = View.VISIBLE
                binding.tpStart.visibility = View.VISIBLE

                binding.npEndDay.visibility = View.GONE
                binding.npEndMonth.visibility = View.GONE
                binding.tpEnd.visibility = View.GONE
            } else {
                binding.npStartDay.visibility = View.GONE
                binding.npStartMonth.visibility = View.GONE
                binding.tpStart.visibility = View.GONE
            }
        }

        binding.clEnd.setOnClickListener {
            isEndPickerClicked = !isEndPickerClicked
            if (isEndPickerClicked) {
                isStartPickerClicked = false
                binding.npEndDay.visibility = View.VISIBLE
                binding.npEndMonth.visibility = View.VISIBLE
                binding.tpEnd.visibility = View.VISIBLE

                binding.npStartDay.visibility = View.GONE
                binding.npStartMonth.visibility = View.GONE
                binding.tpStart.visibility = View.GONE
            } else {
                binding.npEndDay.visibility = View.GONE
                binding.npEndMonth.visibility = View.GONE
                binding.tpEnd.visibility = View.GONE
            }
        }

        // 일정 제목 입력
        binding.etScheduleTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0?.length == 0) {
                    binding.tvScheduleTitleError.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                } else {
                    binding.tvScheduleTitleError.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                }
                scheduleTitle = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        // 일정 메모 입력
        binding.etMemo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                scheduleMemo = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })


    }

    // 해당 월의 일 수 가져오기
    private fun getMaxDayOfMonth(year: Int, month: Int): Int {
        val calendar = java.util.Calendar.getInstance(timeZone)
        calendar.set(year, month - 1, 1) // 연도와 월 설정
        return calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    }

    // 일 설정 업데이트
    private fun updateDayPicker(maxDay: Int) {
        val days = Array(maxDay) { (it + 1).toString() + "일" }
        binding.npStartDay.displayedValues = null // 기존에 할당된 displayedValues 초기화
        binding.npStartDay.minValue = 1
        binding.npStartDay.maxValue = maxDay // 최대 일 수로 설정
        binding.npStartDay.displayedValues = days // displayedValues 배열 재할당

        binding.npEndDay.displayedValues = null
        binding.npEndDay.minValue = 1
        binding.npEndDay.maxValue = maxDay
        binding.npEndDay.displayedValues = days
    }

    // 시작 날짜 설정
    private fun updateStartTimeText(
        year: Int,
        month: Int,
        day: Int,
        week: String,
        time: String?
    ) {
        val startTimeText = if (time == null)
            "${year}년 ${month}월 ${day}일 (${week})"
        else
            "${year}년 ${month}월 ${day}일 (${week})  $time"

        if (time == null) binding.tvStartAllDay.text = startTimeText
        else binding.tvStartTime.text = startTimeText
    }

    // 종료 날짜 설정
    private fun updateEndTimeText(year: Int, month: Int, day: Int, week: String, time: String?) {
        val endTimeText = if (time == null)
            "${year}년 ${month}월 ${day}일 (${week})"
        else
            "${year}년 ${month}월 ${day}일 (${week})  $time"

        if (time == null) binding.tvEndAllDay.text = endTimeText
        else binding.tvEndTime.text = endTimeText
    }


    private fun addSchedule(event: Event, attendees: List<String>?) {
        // Google Calendar API 호출
        getResultsFromApi(event, attendees)
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
    private fun getResultsFromApi(event: Event, attendees: List<String>?): String? {
        if (!isGooglePlayServicesAvailable()) {  // Google Play Services를 사용할 수 없는 경우
            acquireGooglePlayServices()
        } else if (mCredential!!.selectedAccountName == null) {  // 유효한 Google 계정이 선택되어 있지 않은 경우
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
            mCredential!!.selectedAccount = lastSignedInAccount!!.account

            task = MakeRequestTask(
                mCredential,
                event,
                attendees
            )
            task.execute()
        } else if (!isDeviceOnline()) {  // 인터넷을 사용할 수 없는 경우
            Toast.makeText(this, "인터넷을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // Google Calendar API 호출
            task = MakeRequestTask(
                mCredential,
                event,
                attendees
            )
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
        val connMgr =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val networkInfo = connMgr!!.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
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
    private inner class MakeRequestTask(
        credential: GoogleAccountCredential?,
        private var event: Event?,
        private var attendees: List<String>?
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

        private fun onPreExecute() {
//            binding.pbScheduleLoading.show()
        }

        /**
         * 백그라운드에서 Google Calendar API 호출 처리
         */
        fun execute() {
            job = CoroutineScope(Dispatchers.Main).launch {
                onPreExecute()

                try {
                    val result = withContext(Dispatchers.IO) {
                        try {
                            addEvent(event!!, attendees)
                            null
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
        }


        private fun onPostExecute(result: List<GuardianSchedule>?) {
//            binding.pbScheduleLoading.hide()
        }

        private fun onCancelled() {
//            binding.pbScheduleLoading.hide()
        }

        private fun addEvent(event: Event, attendees: List<String>?): String {
            val calendarID: String = getCalendarID("공유 캘린더") ?: return "공유 캘린더를 먼저 생성하세요."
            event.attendees = attendees?.map { email -> EventAttendee().setEmail(email) }
            try {
                val newEvent = mService!!.events().insert(calendarID, event).execute()
                Log.e("[addEvent] NewEvent", newEvent.toString())

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Exception", "Exception : $e")
            }

            return "created : " + event.htmlLink
        }
    }

}