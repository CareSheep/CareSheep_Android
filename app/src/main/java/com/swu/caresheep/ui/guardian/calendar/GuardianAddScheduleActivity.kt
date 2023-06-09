package com.swu.caresheep.ui.guardian.calendar

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.model.*
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianAddScheduleBinding
import com.swu.caresheep.databinding.BottomSheetScheduleNotificationBinding
import com.swu.caresheep.databinding.BottomSheetScheduleRepeatBinding
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.REQUEST_AUTHORIZATION
import com.swu.caresheep.ui.guardian.calendar.GuardianCalendarFragment.Companion.timeZone
import kotlinx.coroutines.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

class GuardianAddScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianAddScheduleBinding

    // Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    private var mService: com.google.api.services.calendar.Calendar? = null
    private var mCredential: GoogleAccountCredential? = null

    private var currentStartYear: Int = 0
    private var currentStartMonth: Int = 0
    private var currentStartDay: Int = 0
    private var currentStartWeek: String = ""
    var currentTime: String = ""
    var currentEndTime: String = ""

    private var currentEndYear: Int = 0
    private var currentEndMonth: Int = 0
    private var currentEndDay: Int = 0
    private var currentEndWeek: String = ""

    private var isStartPickerClicked = false
    private var isEndPickerClicked = false

    private var isTimeTypeClicked = true
    private var isAllDayTypeClicked = false

    private var scheduleTitle: String = ""
    private var scheduleMemo: String = ""
    private var notificationBottomSheetDialog: BottomSheetDialog? = null
    private var repeatBottomSheetDialog: BottomSheetDialog? = null

    private var eventInfo: Event = Event()

    private var task: MakeRequestTask = MakeRequestTask(mCredential, null)

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
        val calendar = Calendar.getInstance(timeZone)
        calendar.time = selectedDate

        currentStartYear = calendar.get(Calendar.YEAR)
        currentStartMonth = calendar.get(Calendar.MONTH) + 1
        currentStartDay = calendar.get(Calendar.DAY_OF_MONTH)
        currentStartWeek = calendar.getDisplayName(
            Calendar.DAY_OF_WEEK,
            Calendar.SHORT,
            Locale.getDefault()
        )!!

        currentEndYear = calendar.get(Calendar.YEAR)
        currentEndMonth = calendar.get(Calendar.MONTH) + 1
        currentEndDay = calendar.get(Calendar.DAY_OF_MONTH)
        currentEndWeek = calendar.getDisplayName(
            Calendar.DAY_OF_WEEK,
            Calendar.SHORT,
            Locale.getDefault()
        )!!

        // TimePicker 초기 설정
        binding.tpStart.hour = calendar.get(Calendar.HOUR_OF_DAY)  // 시작 Time
        binding.tpStart.minute = calendar.get(Calendar.MINUTE)
        binding.tpEnd.hour = calendar.get(Calendar.HOUR_OF_DAY) + 1  // 종료 Time
        binding.tpEnd.minute = calendar.get(Calendar.MINUTE)


        val strCurrentMinute =
            if (calendar.get(Calendar.MINUTE) / 10 == 0) "0${calendar.get(Calendar.MINUTE)}" else calendar.get(
                Calendar.MINUTE
            )
        val strCurrentAMPM =
            if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전"
            else "오후"
        val strCurrentHour12 =
            if (calendar.get(Calendar.HOUR_OF_DAY) == 0) 12 else if (calendar.get(Calendar.HOUR_OF_DAY) > 12) calendar.get(
                Calendar.HOUR_OF_DAY
            ) - 12 else calendar.get(Calendar.HOUR_OF_DAY)

        val strCurrentEndAMPM =
            if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전"
            else "오후"

        val strCurrentEndHour12 =
            if (calendar.get(Calendar.HOUR_OF_DAY) + 1 > 12) calendar.get(
                Calendar.HOUR_OF_DAY
            ) + 1 - 12 else calendar.get(Calendar.HOUR_OF_DAY) + 1

        currentTime = "$strCurrentAMPM $strCurrentHour12:$strCurrentMinute"
        currentEndTime = "$strCurrentEndAMPM $strCurrentEndHour12:$strCurrentMinute"

        // 시작 TimePicker 시간 변경 시
        binding.tpStart.setOnTimeChangedListener { _, newHour, newMinute ->
            val strNewAmPm: String = if (newHour >= 12) "오후" else "오전"

            val strNewHour =
                if (newHour == 0) 12 else if (newHour > 12) newHour - 12 else newHour

            val strNewMinute =
                if (calendar.get(Calendar.MINUTE) / 10 == 0) "0${newMinute}" else newMinute.toString()

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
                if (calendar.get(Calendar.MINUTE) / 10 == 0) "0${newMinute}" else newMinute.toString()

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
            isTimeTypeClicked = true
            isAllDayTypeClicked = false
            binding.btnTime.isSelected = isTimeTypeClicked
            binding.btnAllDay.isSelected = isAllDayTypeClicked

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
            isTimeTypeClicked = false
            isAllDayTypeClicked = true
            binding.btnTime.isSelected = isTimeTypeClicked
            binding.btnAllDay.isSelected = isAllDayTypeClicked

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
                currentEndYear,
                currentEndMonth,
                currentEndDay,
                currentEndWeek,
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
            val startCalendar: Calendar
            val startDateTime: DateTime

            if (isAllDayTypeClicked && !isTimeTypeClicked) {
                startCalendar = Calendar.getInstance(timeZone).apply {
                    set(
                        currentStartYear,
                        currentStartMonth - 1,
                        currentStartDay
                    )
                }
                startDateTime = DateTime(startCalendar.time)
            } else {
                startCalendar = Calendar.getInstance(timeZone).apply {
                    set(
                        currentStartYear,
                        currentStartMonth - 1,
                        currentStartDay,
                        binding.tpStart.hour,
                        binding.tpStart.minute
                    )
                }
                startDateTime = DateTime(startCalendar.time)
            }

            // 종료 시간 가져오기
            val endCalendar: Calendar
            val endDateTime: DateTime

            if (isAllDayTypeClicked && !isTimeTypeClicked) {
                endCalendar = Calendar.getInstance(timeZone).apply {
                    set(
                        currentEndYear,
                        currentEndMonth - 1,
                        currentEndDay
                    )
                }
                endDateTime = DateTime(endCalendar.time)
            } else {
                endCalendar = Calendar.getInstance(timeZone).apply {
                    set(
                        currentEndYear,
                        currentEndMonth - 1,
                        currentEndDay,
                        binding.tpEnd.hour,
                        binding.tpEnd.minute,
                    )
                }
                endDateTime = DateTime(endCalendar.time)
            }

            // 일정 추가
            lifecycleScope.launch {
                Log.e("startDateTime", startDateTime.toString())
                getNewEventInfo(scheduleTitle, scheduleMemo, startDateTime, endDateTime)

                addSchedule(eventInfo)
            }

            val sharedPrefs = getSharedPreferences("Add Schedule", Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean("isAdded", true).apply()

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
    ) {
        eventInfo.setSummary(title).description = memo

        // 시작 시간
        val start = EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone("Asia/Seoul")

        if (isAllDayTypeClicked && !isTimeTypeClicked) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(startDateTime.value))

            val startAllDayDateTime = DateTime(formattedDate)
            if (eventInfo.start == null) {
                eventInfo.start = EventDateTime().setDate(startAllDayDateTime)
            } else {
                eventInfo.start.date = startAllDayDateTime
            }
        } else {
            eventInfo.start = start
        }


        // 종료 시간
        val end = EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone("Asia/Seoul")
        if (isAllDayTypeClicked && !isTimeTypeClicked) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(endDateTime.value))

            val endAllDayDateTime = DateTime(formattedDate)
            if (eventInfo.end == null) {
                eventInfo.end = EventDateTime().setDate(endAllDayDateTime)
            } else {
                eventInfo.end.date = endAllDayDateTime
                eventInfo.end.dateTime = null
            }
        } else {
//            eventInfo.end = end.setDateTime(endDateTime)
            eventInfo.end = end
        }
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
        binding.btnTime.isSelected = isTimeTypeClicked
        binding.btnAllDay.isSelected = isAllDayTypeClicked

        // 날짜 및 시간 선택 visibility 설정
        binding.clStart.setOnClickListener {
            if (isTimeTypeClicked) {
                // 시간 유형
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

            } else if (isAllDayTypeClicked) {
                // 종일 유형
                val datePickerDialog =
                    DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                        val calendar = Calendar.getInstance(timeZone)
                        calendar.set(selectedYear, selectedMonth, selectedDay)

                        val selectedWeek = calendar.getDisplayName(
                            Calendar.DAY_OF_WEEK,
                            Calendar.SHORT,
                            Locale.getDefault()
                        )!!

                        currentStartYear = selectedYear
                        currentStartMonth = selectedMonth + 1
                        currentStartDay = selectedDay
                        currentStartWeek = selectedWeek

                        currentEndYear = selectedYear
                        currentEndMonth = selectedMonth + 1
                        currentEndDay = selectedDay
                        currentEndWeek = selectedWeek


                        updateStartTimeText(
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay,
                            selectedWeek,
                            null
                        )

                        updateEndTimeText(
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay,
                            selectedWeek,
                            null
                        )

                        // 갱신
                        binding.npStartMonth.value = currentStartMonth
                        binding.npStartDay.value = currentStartDay

                        updateStartTimeText(currentStartYear, currentStartMonth, currentStartDay, currentStartWeek, currentTime)

                        binding.npEndMonth.value = currentEndMonth
                        binding.npEndDay.value = currentEndDay

                        updateEndTimeText(currentEndYear, currentEndMonth, currentEndDay, currentEndWeek, currentEndTime)

                    }, currentStartYear, currentStartMonth - 1, currentStartDay)


                // DatePickerDialog를 보여줌
                datePickerDialog.show()


            }
        }

        binding.clEnd.setOnClickListener {
            if (isTimeTypeClicked) {
                // 시간 유형
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

            } else if (isAllDayTypeClicked) {
                // 종일 유형
                val datePickerDialog =
                    DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                        val calendar = Calendar.getInstance(timeZone)
                        calendar.set(selectedYear, selectedMonth, selectedDay)

                        val selectedWeek = calendar.getDisplayName(
                            Calendar.DAY_OF_WEEK,
                            Calendar.SHORT,
                            Locale.getDefault()
                        )!!

                        currentEndYear = selectedYear
                        currentEndMonth = selectedMonth + 1
                        currentEndDay = selectedDay
                        currentEndWeek = selectedWeek

                        currentStartYear = selectedYear
                        currentStartMonth = selectedMonth + 1
                        currentStartDay = selectedDay
                        currentStartWeek = selectedWeek

                        updateStartTimeText(
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay,
                            selectedWeek,
                            null
                        )
                        updateEndTimeText(
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay,
                            selectedWeek,
                            null
                        )

                        // 갱신
                        binding.npStartMonth.value = currentStartMonth
                        binding.npStartDay.value = currentStartDay

                        updateStartTimeText(currentStartYear, currentStartMonth, currentStartDay, currentStartWeek, currentTime)

                        binding.npEndMonth.value = currentEndMonth
                        binding.npEndDay.value = currentEndDay

                        updateEndTimeText(currentEndYear, currentEndMonth, currentEndDay, currentEndWeek, currentEndTime)

                    }, currentEndYear, currentEndMonth - 1, currentEndDay)


                // DatePickerDialog를 보여줌
                datePickerDialog.show()

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


        // 알림
        // 알림 메뉴 Bottom Sheet
        val notificationBottomSheetView =
            layoutInflater.inflate(R.layout.bottom_sheet_schedule_notification, binding.root, false)
        notificationBottomSheetDialog =
            BottomSheetDialog(this, R.style.BottomSheetDialogCustom)

        notificationBottomSheetDialog!!.setContentView(notificationBottomSheetView!!)
        setNotificationBottomSheetView(
            notificationBottomSheetView,
            notificationBottomSheetDialog!!
        )

        binding.clAlarm.setOnClickListener {
            notificationBottomSheetDialog!!.show()
        }


        // 반복
        // 반복 메뉴 Bottom Sheet
        val repeatBottomSheetView =
            layoutInflater.inflate(R.layout.bottom_sheet_schedule_repeat, binding.root, false)
        repeatBottomSheetDialog =
            BottomSheetDialog(this, R.style.BottomSheetDialogCustom)

        repeatBottomSheetDialog!!.setContentView(repeatBottomSheetView!!)
        setRepeatBottomSheetView(repeatBottomSheetView, repeatBottomSheetDialog!!)

        binding.clRepeat.setOnClickListener {
            repeatBottomSheetDialog!!.show()
        }


    }

    // 알림 메뉴 Bottom Sheet Click event 설정
    private fun setNotificationBottomSheetView(
        bottomSheetView: View,
        dialog: BottomSheetDialog
    ) {
        val notificationBinding = BottomSheetScheduleNotificationBinding.bind(bottomSheetView)

        // 기본으로 10분 전으로 설정
        var reminders = Event.Reminders()
        reminders.useDefault = false

        var reminder10Minutes = EventReminder()
        reminder10Minutes.method = "popup"
        reminder10Minutes.minutes = 10

        reminders.overrides = listOf(reminder10Minutes)

        eventInfo.reminders = reminders

        // 알림 없음
        notificationBinding.tvBottomSheetNotificationNone.setOnClickListener {
            binding.tvAlarm.text = notificationBinding.tvBottomSheetNotificationNone.text

            reminders = Event.Reminders()
            reminders.useDefault = true
            eventInfo.reminders = reminders

            dialog.dismiss()
        }

        // 일정 시작시간
        notificationBinding.tvBottomSheetNotificationStart.setOnClickListener {
            binding.tvAlarm.text = notificationBinding.tvBottomSheetNotificationStart.text

            reminders = Event.Reminders()
            reminders.useDefault = false

            val reminderAtStart = EventReminder()
            reminderAtStart.method = "popup"
            reminderAtStart.minutes = 0

            reminders.overrides = listOf(reminderAtStart)

            eventInfo.reminders = reminders

            dialog.dismiss()
        }

        // 10분 전
        notificationBinding.tvBottomSheetNotificationMinute.setOnClickListener {
            binding.tvAlarm.text = notificationBinding.tvBottomSheetNotificationMinute.text

            reminders = Event.Reminders()
            reminders.useDefault = false

            reminder10Minutes = EventReminder()
            reminder10Minutes.method = "popup"
            reminder10Minutes.minutes = 10

            reminders.overrides = listOf(reminder10Minutes)

            eventInfo.reminders = reminders

            dialog.dismiss()
        }

        // 1시간 전
        notificationBinding.tvBottomSheetNotificationHour.setOnClickListener {
            binding.tvAlarm.text = notificationBinding.tvBottomSheetNotificationHour.text

            reminders = Event.Reminders()
            reminders.useDefault = false

            val reminder1Hour = EventReminder()
            reminder1Hour.method = "popup"
            reminder1Hour.minutes = 60

            reminders.overrides = listOf(reminder1Hour)

            eventInfo.reminders = reminders

            dialog.dismiss()
        }

        // 1일 전
        notificationBinding.tvBottomSheetNotificationDay.setOnClickListener {
            binding.tvAlarm.text = notificationBinding.tvBottomSheetNotificationDay.text

            reminders = Event.Reminders()
            reminders.useDefault = false

            val reminder1Day = EventReminder()
            reminder1Day.method = "popup"
            reminder1Day.minutes = 24 * 60

            reminders.overrides = listOf(reminder1Day)

            eventInfo.reminders = reminders

            dialog.dismiss()
        }

        // 닫기
        notificationBinding.btnBottomSheetNotificationClose.setOnClickListener {
            dialog.dismiss()
        }

    }

    // 반복 메뉴 Bottom Sheet Click event 설정
    private fun setRepeatBottomSheetView(
        bottomSheetView: View,
        dialog: BottomSheetDialog
    ) {
        val repeatBinding = BottomSheetScheduleRepeatBinding.bind(bottomSheetView)

        // 기본으로 반복 없음 설정
        eventInfo.recurrence = null

        // 반복 없음
        repeatBinding.tvBottomSheetRepeatNone.setOnClickListener {
            binding.tvRepeat.text = repeatBinding.tvBottomSheetRepeatNone.text

            eventInfo.recurrence = null

            dialog.dismiss()
        }

        // 매일
        repeatBinding.tvBottomSheetRepeatDay.setOnClickListener {
            binding.tvRepeat.text = repeatBinding.tvBottomSheetRepeatDay.text

            eventInfo.recurrence = listOf("RRULE:FREQ=DAILY")

            dialog.dismiss()
        }

        // 매주
        repeatBinding.tvBottomSheetRepeatWeek.setOnClickListener {
            binding.tvRepeat.text = repeatBinding.tvBottomSheetRepeatWeek.text

            eventInfo.recurrence = listOf("RRULE:FREQ=WEEKLY")

            dialog.dismiss()
        }

        // 매월
        repeatBinding.tvBottomSheetRepeatMonth.setOnClickListener {
            binding.tvRepeat.text = repeatBinding.tvBottomSheetRepeatMonth.text

            eventInfo.recurrence = listOf("RRULE:FREQ=MONTHLY")

            dialog.dismiss()
        }

        // 매년
        repeatBinding.tvBottomSheetRepeatYear.setOnClickListener {
            binding.tvRepeat.text = repeatBinding.tvBottomSheetRepeatYear.text

            eventInfo.recurrence = listOf("RRULE:FREQ=YEARLY")

            dialog.dismiss()
        }

        // 닫기
        repeatBinding.btnBottomSheetRepeatClose.setOnClickListener {
            dialog.dismiss()
        }

    }


    // 해당 월의 일 수 가져오기
    private fun getMaxDayOfMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance(timeZone)
        calendar.set(year, month - 1, 1) // 연도와 월 설정
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
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


    private fun addSchedule(event: Event) {
        // Google Calendar API 호출
        getResultsFromApi(event)
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
    private fun getResultsFromApi(event: Event): String? {
        if (!isGooglePlayServicesAvailable()) {  // Google Play Services를 사용할 수 없는 경우
            acquireGooglePlayServices()
        } else if (mCredential!!.selectedAccountName == null) {  // 유효한 Google 계정이 선택되어 있지 않은 경우
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
            mCredential!!.selectedAccount = lastSignedInAccount!!.account

            task = MakeRequestTask(
                mCredential,
                event
            )
            task.execute()
        } else if (!isDeviceOnline()) {  // 인터넷을 사용할 수 없는 경우
            Toast.makeText(this, "인터넷을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // Google Calendar API 호출
            task = MakeRequestTask(
                mCredential,
                event
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
    private inner class MakeRequestTask(
        credential: GoogleAccountCredential?,
        private var event: Event?
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
                    val result = withContext(Dispatchers.IO) {
                        try {
                            addEvent(event!!)
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

        private fun addEvent(event: Event): String {
            val calendarID: String = getCalendarID("공유 캘린더") ?: return "공유 캘린더를 먼저 생성하세요."
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