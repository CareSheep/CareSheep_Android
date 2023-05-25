package com.swu.caresheep.ui.guardian.home

import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
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
import com.google.api.services.calendar.model.*
import com.swu.caresheep.R
import com.swu.caresheep.databinding.FragmentGuardianHomeBinding
import com.swu.caresheep.recyclerview.RecycleMainRecordActivity
import com.swu.caresheep.ui.guardian.GuardianElderReportActivity
import com.swu.caresheep.ui.guardian.calendar.*
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.PREF_ACCOUNT_NAME
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.REQUEST_AUTHORIZATION
import com.swu.caresheep.ui.start.user_id
import com.swu.caresheep.utils.GoogleLoginClient
import kotlinx.coroutines.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class GuardianHomeFragment : Fragment() {

    private lateinit var binding: FragmentGuardianHomeBinding

    // Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    private var mService: com.google.api.services.calendar.Calendar? = null
    var mCredential: GoogleAccountCredential? = null

    private var googleLoginClient: GoogleLoginClient = GoogleLoginClient()
    private var task: MakeRequestTask = MakeRequestTask(mCredential, null)
    private var elderGmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuardianHomeBinding.inflate(inflater, container, false)

        // 당겨서 새로고침 기능 세팅
        binding.layoutSwipeRefresh.setOnRefreshListener {
            updateTodaySchedule()
            binding.layoutSwipeRefresh.isRefreshing = false
        }

        // 스크롤 업 대신에 리프레쉬 이벤트가 트리거 되는걸 방지하기 위해서
        // scrollView의 scroll y축이 0, 즉 최상단에 위치했을 때만 refreshLayout을 활성화
        binding.layoutScroll.viewTreeObserver.addOnScrollChangedListener {
            binding.layoutSwipeRefresh.isEnabled = (binding.layoutScroll.scrollY == 0)
        }

        binding.clAnalysisResult.setOnClickListener {
            // 리포트 확인 화면으로 이동
            val intent = Intent(requireContext(), GuardianElderReportActivity::class.java)
            startActivity(intent)
        }

        binding.clVoiceMailbox.setOnClickListener {
            // 음성 사서함 목록 화면으로 이동
            val intent = Intent(requireContext(), RecycleMainRecordActivity::class.java)
            startActivity(intent)
        }

        updateTodaySchedule()

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        task.job?.cancel()
    }

    override fun onStart() {
        super.onStart()
//        if (elderGmail == null) {
//            lifecycleScope.launch {
//                val elderInfo =
//                    withContext(Dispatchers.IO) { googleLoginClient.getElderInfo(requireContext()) }
//                elderGmail = elderInfo.gmail
//
//                if (elderGmail == null) {
//                    Toast.makeText(
//                        requireContext(),
//                        "사용자 코드를 입력하여 어르신과 연결하세요.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    startActivity(Intent(requireContext(), GuardianConnectActivity::class.java))
//                } else {
//                    // 오늘의 일정 불러오기
//                    updateTodaySchedule()
//                }
//            }
//        } else {
//            updateTodaySchedule()
//
//        }
    }

    private fun updateTodaySchedule() {
        val today = java.util.Calendar.getInstance()

        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
            context,
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
    private fun getResultsFromApi(selectedDate: java.util.Calendar?): String? {
        if (!isGooglePlayServicesAvailable()) {  // Google Play Services를 사용할 수 없는 경우
            acquireGooglePlayServices()
        } else if (mCredential!!.selectedAccountName == null) {  // 유효한 Google 계정이 선택되어 있지 않은 경우
            // 구글 계정 연결 후 Google Calendar API 호출
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
            mCredential!!.selectedAccount = lastSignedInAccount!!.account

            task = MakeRequestTask(mCredential, selectedDate)
            task.execute()
        } else if (!isDeviceOnline()) {  // 인터넷을 사용할 수 없는 경우
            Toast.makeText(requireContext(), "인터넷을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // Google Calendar API 호출
            task = MakeRequestTask(mCredential, selectedDate)
            task.execute()
        }
        return null
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
    fun showGooglePlayServicesAvailabilityErrorDialog(
        connectionStatusCode: Int
    ) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog: Dialog = apiAvailability.getErrorDialog(
            requireActivity(),
            connectionStatusCode,
            GuardianCalendarFragment.REQUEST_GOOGLE_PLAY_SERVICES
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
            GuardianCalendarFragment.REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(
                    requireContext(),
                    "앱을 실행시키려면 구글 플레이 서비스가 필요합니다. 구글 플레이 서비스를 설치 후 다시 실행하세요.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                getResultsFromApi(null)
            }
            GuardianCalendarFragment.REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
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
            requireContext()
        )
    }

    /**
     * 안드로이드 디바이스가 인터넷 연결되어 있는지 확인한다. 연결되어 있다면 True 리턴, 아니면 False 리턴
     */
    private fun isDeviceOnline(): Boolean {
        val connMgr =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
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
        private var selectedDate: java.util.Calendar?
    ) {
        private var mLastError: Exception? = null
        var job: Job? = null

        // 일정 데이터 리스트 선언
        var todayScheduleData = ArrayList<GuardianSchedule>()
        val todayScheduleRVAdapter = GuardianTodayScheduleRVAdapter(todayScheduleData)

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

            binding.tvTodayScheduleNotExist.visibility = View.INVISIBLE
            binding.ivTodayScheduleNotExist.visibility = View.INVISIBLE

            binding.pbScheduleLoading.show()
        }

        /**
         * CalendarTitle 이름의 캘린더에서 해당 날짜의 일정을 가져와 리턴
         */
        @Throws(IOException::class)
        private fun getEvent(selectedDate: java.util.Calendar?): List<GuardianSchedule>? {
            // 오늘 날짜로부터 시작과 끝 시간을 계산
            val testDate: java.util.Calendar = selectedDate ?: java.util.Calendar.getInstance()

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
                // 공유 캘린더가 없으므로 오늘의 일정 X -> RV 안보이게 설정
                binding.tvTodayScheduleNotExist.visibility = View.VISIBLE
                binding.ivTodayScheduleNotExist.visibility = View.VISIBLE
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
            val scheduleData = ArrayList<GuardianSchedule>()

            // CalendarView에 일정 표시
            val calendar = java.util.Calendar.getInstance()
            items.forEach { event ->
                var eventTitle = event.summary
                if (eventTitle.isNullOrEmpty()) {
                    eventTitle = "(제목 없음)"
                }
                val start = event.start.dateTime
                val end = event.end.dateTime

                var startTime = ""
                var endTime = ""

                if (start != null) {
                    // 한국 시간대로 설정
                    val koreaTimeZone = TimeZone.getTimeZone("Asia/Seoul")
                    calendar.timeZone = koreaTimeZone

                    // 일정 시작 시간 계산
                    val startDate = Date(start.value)
                    calendar.time = startDate
                    val startHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                    val startMinute = calendar.get(java.util.Calendar.MINUTE)
                    val startAMPM = calendar.get(java.util.Calendar.AM_PM)

                    val strStartMinute = if (startMinute / 10 == 0) "0$startMinute" else startMinute
                    val strStartAMPM = if (startAMPM == java.util.Calendar.AM) "오전"
                    else "오후"
                    val startHour12 =
                        if (startHour == 0) 12 else if (startHour > 12) startHour - 12 else startHour

                    startTime = "$strStartAMPM ${startHour12}:${strStartMinute}"

                    // 일정 종료 시간 계산
                    val endDate = Date(end.value)
                    calendar.time = endDate
                    val endHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                    val endMinute = calendar.get(java.util.Calendar.MINUTE)
                    val endAMPM = calendar.get(java.util.Calendar.AM_PM)

                    val strEndMinute = if (endMinute / 10 == 0) "0$endMinute" else endMinute
                    val strEndAMPM = if (endAMPM == java.util.Calendar.AM) "오전"
                    else "오후"
                    val endHour12 =
                        if (endHour == 0) 12 else if (endHour > 12) endHour - 12 else endHour

                    endTime = "$strEndAMPM ${endHour12}:${strEndMinute}"

                }
                scheduleData.add(GuardianSchedule(startTime, endTime, eventTitle))
            }

            Log.e("calendar", scheduleData.size.toString() + "개의 데이터를 가져왔습니다.")

            return scheduleData
        }

        private fun onPostExecute(result: List<GuardianSchedule>?) {

            result?.let {
                if (result.isNotEmpty()) {
                    // RecyclerView 어댑터와 데이터 리스트 연결
                    val todayScheduleRVAdapter =
                        GuardianTodayScheduleRVAdapter(it as ArrayList<GuardianSchedule>)
                    binding.rvTodaySchedule.adapter = todayScheduleRVAdapter

                    // 오늘의 일정 있으므로 RV 보이게 설정
                    binding.tvTodayScheduleNotExist.visibility = View.INVISIBLE
                    binding.ivTodayScheduleNotExist.visibility = View.INVISIBLE
                    binding.rvTodaySchedule.visibility = View.VISIBLE
                } else {
                    // 오늘의 일정 없으므로 RV 안 보이게 설정
                    binding.tvTodayScheduleNotExist.visibility = View.VISIBLE
                    binding.ivTodayScheduleNotExist.visibility = View.VISIBLE
                    binding.rvTodaySchedule.visibility = View.INVISIBLE


                    val context = activity?.applicationContext
                    val resources = context?.resources
                    val animation =
                        resources?.let { AnimationUtils.loadAnimation(context, R.anim.fade_in) }

                    animation?.also { hyperspaceJumpAnimation ->
                        binding.tvTodayScheduleNotExist.startAnimation(hyperspaceJumpAnimation)
                        binding.ivTodayScheduleNotExist.startAnimation(hyperspaceJumpAnimation)
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
            }
        }

    }
}