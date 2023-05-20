package com.swu.caresheep.ui.guardian.calendar

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.*
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianAddScheduleBinding
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.timeZone
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GuardianAddScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianAddScheduleBinding

    // Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    private var mService: com.google.api.services.calendar.Calendar? = null
    var mCredential: GoogleAccountCredential? = null
    var isStartPickerClicked = false
    var isEndPickerClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // GuardianCalendarFragment에서 선택된 날짜 가져오기
        val sharedPreferences = getSharedPreferences("SelectedDate", MODE_PRIVATE)
        val selectedDateInMillis = sharedPreferences.getLong("selectedDate", 0)
        val selectedDate = if (selectedDateInMillis != 0L) Date(selectedDateInMillis) else Date()


        val calendar = java.util.Calendar.getInstance(timeZone)
        calendar.time = selectedDate

        var currentYear = calendar.get(java.util.Calendar.YEAR)
        var currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
        var currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val currentWeek = calendar.getDisplayName(
            java.util.Calendar.DAY_OF_WEEK,
            java.util.Calendar.SHORT,
            Locale.getDefault()
        )!!

        val strCurrentMinute = if (calendar.get(java.util.Calendar.MINUTE) / 10 == 0) "0${calendar.get(java.util.Calendar.MINUTE)}" else calendar.get(java.util.Calendar.MINUTE)

        val strCurrentAMPM = if (calendar.get(java.util.Calendar.AM_PM) == 1) "오전"
        else "오후"
        val strCurrentHour12 =
            if (calendar.get(java.util.Calendar.HOUR_OF_DAY) == 0) 12 else if (calendar.get(java.util.Calendar.HOUR_OF_DAY) > 12) calendar.get(java.util.Calendar.HOUR_OF_DAY) - 12 else calendar.get(java.util.Calendar.HOUR_OF_DAY)

        val currentTime = "$strCurrentAMPM $strCurrentHour12:$strCurrentMinute"


        // 월 설정
        val months = arrayOf(
            "1월", "2월", "3월", "4월", "5월", "6월",
            "7월", "8월", "9월", "10월", "11월", "12월"
        )
        binding.npStartMonth.displayedValues = months
        binding.npStartMonth.minValue = 1
        binding.npStartMonth.maxValue = months.size

        // 일 설정
        val maxDay = getMaxDayOfMonth(currentYear, currentMonth)
        updateDayPicker(maxDay)


        // 월 변경 시 연도 변경
        binding.npStartMonth.setOnValueChangedListener { _, _, newMonth ->
            if (newMonth == 1 && currentMonth == 12) { // 12월에서 1월로 변경할 경우
                // 연도 전환
                currentYear++
            } else if (newMonth == 12 && currentMonth == 1) { // 1월에서 12월로 변경할 경우
                // 연도 전환
                currentYear--
            }
            // 현재 월 갱신
            currentMonth = newMonth

            // 일 설정
            updateDayPicker(getMaxDayOfMonth(currentYear, currentMonth))

            // 시작 시간 업데이트
            updateStartTimeText(currentYear, currentMonth, currentDay, currentWeek, currentTime)
        }

        // 일 변경
        binding.npStartDay.setOnValueChangedListener { _, _, newDay ->
            currentDay = newDay

            // 시작 시간 업데이트
            updateStartTimeText(currentYear, currentMonth, currentDay, currentWeek, currentTime)
        }

        // 초기 상태 설정
        binding.npStartMonth.value = currentMonth
        binding.npStartDay.value = currentDay
        updateStartTimeText(currentYear, currentMonth, currentDay, currentWeek, currentTime)



        binding.btnTime.isSelected = true
        binding.btnAllDay.isSelected = false

        // 시간 선택 시
        binding.btnTime.setOnClickListener {
            binding.btnTime.isSelected = true
            binding.btnAllDay.isSelected = false
        }

        // 종일 선택 시
        binding.btnAllDay.setOnClickListener {
            binding.btnTime.isSelected = false
            binding.btnAllDay.isSelected = true

            binding.npStartDay.visibility = View.GONE
            binding.npStartMonth.visibility = View.GONE
            binding.tpStart.visibility = View.GONE

            updateStartTimeText(currentYear, currentMonth, currentDay, currentWeek, null)
            updateEndTimeText(currentYear, currentMonth, currentDay, currentWeek)
        }

        // 날짜 및 시간 선택 visibility 설정
        binding.clStart.setOnClickListener {
            isStartPickerClicked = !isStartPickerClicked
            if (isStartPickerClicked) {
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


    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        // 뒤로 가기
        binding.ivBack.setOnClickListener { finish() }
        // 취소 버튼 클릭 시
        binding.btnCancel.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
        super.onDestroy()
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
    }

    // 시작 날짜 설정
    private fun updateStartTimeText(year: Int, month: Int, day: Int, week: String, currentTime: String?) {
        val startTimeText = if (currentTime == null)
            "${year}년 ${month}월 ${day}일 (${week})"
        else
            "${year}년 ${month}월 ${day}일 (${week})  $currentTime"
        binding.tvStartTime.text = startTimeText
    }

    // 종료 날짜 설정
    private fun updateEndTimeText(year: Int, month: Int, day: Int, week: String) {
        val endTimeText = "${year}년 ${month}월 ${day}일 (${week})"
        binding.tvEndTime.text = endTimeText
    }



    fun addSchedule() {
//        val selectedDate =
//        // Google Calendar API 호출
//        MakeRequestTask(
//            this,
//            mCredential,
//            selectedDate
//        ).execute()
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
    private inner class MakeRequestTask(
        private val mActivity: GuardianAddScheduleActivity,
        credential: GoogleAccountCredential?,
        private var selectedDate: java.util.Calendar?
    ) :
        AsyncTask<Void?, Void?, List<GuardianSchedule>?>() {
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

        override fun onPreExecute() {
//            binding.pbScheduleLoading.show()
        }

        /**
         * 백그라운드에서 Google Calendar API 호출 처리
         */
        override fun doInBackground(vararg params: Void?): List<GuardianSchedule>? {
            return try {
                addEvent()
                null
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
                null
            }
        }


        override fun onPostExecute(result: List<GuardianSchedule>?) {
            super.onPostExecute(result)

//            binding.pbScheduleLoading.hide()
        }

        override fun onCancelled() {
//            binding.pbScheduleLoading.hide()

        }

        private fun addEvent(): String {
            val calendarID: String = getCalendarID("공유 캘린더") ?: return "캘린더를 먼저 생성하세요."
            var event: Event = Event()
                .setSummary("구글 캘린더 테스트")
                .setLocation("서울시")
                .setDescription("캘린더에 이벤트 추가하는 것을 테스트합니다.")
            val calander: java.util.Calendar = java.util.Calendar.getInstance()
            //simpledateformat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ", Locale.KOREA);
            // Z에 대응하여 +0900이 입력되어 문제 생겨 수작업으로 입력
            val simpledateformat: SimpleDateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+09:00", Locale.KOREA)
            val datetime: String = simpledateformat.format(calander.time)
            val startDateTime =
                DateTime(datetime)
            val start = EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Seoul")
            event.start = start
            Log.d("@@@", datetime)
            val endDateTime =
                DateTime(datetime)
            val end = EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Seoul")
            event.end = end

            //String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=2"};
            //event.setRecurrence(Arrays.asList(recurrence));
            try {
                event = mService!!.events().insert(calendarID, event).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Exception", "Exception : $e")
            }
            System.out.printf("Event created: %s\n", event.htmlLink)
            Log.e("Event", "created : " + event.htmlLink)
            return "created : " + event.htmlLink
        }
    }

}