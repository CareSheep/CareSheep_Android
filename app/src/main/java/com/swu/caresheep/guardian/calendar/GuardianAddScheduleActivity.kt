package com.swu.caresheep.guardian.calendar

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.*
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianAddScheduleBinding
import com.swu.caresheep.databinding.BottomSheetScheduleNotificationBinding
import com.swu.caresheep.databinding.BottomSheetScheduleRepeatBinding
import com.swu.caresheep.utils.calendar.CalendarUtil
import com.swu.caresheep.utils.calendar.CalendarUtil.Companion.SEOUL_TIME_ZONE
import com.swu.caresheep.utils.calendar.CalendarUtil.Companion.SEOUL_TIME_ZONE_ID
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

class GuardianAddScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianAddScheduleBinding
    private lateinit var calendarUtil: CalendarUtil

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

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
            overridePendingTransition(R.anim.none, R.anim.slide_out_right)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        calendarUtil = CalendarUtil(this, null, this, binding)

        calendarUtil.setupGoogleApi()

        // TimePicker 초기 설정 / GuardianCalendarFragment에서 선택된 날짜 가져오기
        setupTimePickers(setupSelectedDate())

        // 월 초기 설정
        val months = Array(12) { (it + 1).toString() + "월" }
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
            val isChangingToJanuary = newMonth == 1 && currentStartMonth == 12  // 12월에서 1월로 변경할 경우
            val isChangingToDecember = newMonth == 12 && currentStartMonth == 1  // 1월에서 12월로 변경할 경우

            if (isChangingToJanuary) {
                currentStartYear++  // 연도 증가
            } else if (isChangingToDecember) {
                currentStartYear--  // 연도 감소
            }

            // 현재 월 갱신
            currentStartMonth = newMonth

            // 일 설정
            val maxDayOfMonth = getMaxDayOfMonth(currentStartYear, currentStartMonth)
            updateDayPicker(maxDayOfMonth)

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
            val isChangingToJanuary = newMonth == 1 && currentEndMonth == 12  // 12월에서 1월로 변경할 경우
            val isChangingToDecember = newMonth == 12 && currentEndMonth == 1  // 1월에서 12월로 변경할 경우

            if (isChangingToJanuary) {
                currentEndYear++  // 연도 증가
            } else if (isChangingToDecember) {
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
                startCalendar = Calendar.getInstance(SEOUL_TIME_ZONE).apply {
                    set(
                        currentStartYear,
                        currentStartMonth - 1,
                        currentStartDay
                    )
                }
                startDateTime = DateTime(startCalendar.time)
            } else {
                startCalendar = Calendar.getInstance(SEOUL_TIME_ZONE).apply {
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
                endCalendar = Calendar.getInstance(SEOUL_TIME_ZONE).apply {
                    set(
                        currentEndYear,
                        currentEndMonth - 1,
                        currentEndDay
                    )
                }
                endDateTime = DateTime(endCalendar.time)
            } else {
                endCalendar = Calendar.getInstance(SEOUL_TIME_ZONE).apply {
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


    /**
     * 선택된 날짜로 초기 설정
     */
    private fun setupSelectedDate(): Date {
        // GuardianCalendarFragment에서 선택된 날짜 가져오기
        val selectedDateInMillis = getSharedPreferences("SelectedDate", MODE_PRIVATE)
            .getLong("selectedDate", 0)
        val selectedDate = if (selectedDateInMillis != 0L) Date(selectedDateInMillis) else Date()

        // 선택된 날짜로 초기 설정
        val calendar = Calendar.getInstance(SEOUL_TIME_ZONE)
        calendar.time = selectedDate

        currentStartYear = calendar.get(Calendar.YEAR)
        currentStartMonth = calendar.get(Calendar.MONTH) + 1
        currentStartDay = calendar.get(Calendar.DAY_OF_MONTH)
        currentStartWeek = calendar.getDisplayName(
            Calendar.DAY_OF_WEEK,
            Calendar.SHORT,
            Locale.getDefault()
        )!!

        currentEndYear = currentStartYear
        currentEndMonth = currentStartMonth
        currentEndDay = currentStartDay
        currentEndWeek = currentStartWeek

        return selectedDate
    }

    /**
     * TimePicker 초기 설정 및 시간 변경 시 작동
     */
    private fun setupTimePickers(selectedDate: Date) {
        val calendar = Calendar.getInstance(SEOUL_TIME_ZONE)
        calendar.time = selectedDate

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // 시작 Time
        binding.tpStart.apply {
            hour = currentHour
            minute = currentMinute
        }

        // 종료 Time
        binding.tpEnd.apply {
            hour = currentHour + 1
            minute = currentMinute
        }

        val strCurrentMinute =
            if (currentMinute / 10 == 0) "0$currentMinute" else currentMinute.toString()
        val strCurrentAMPM = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        val strCurrentHour12 =
            if (currentHour == 0) 12 else if (currentHour > 12) currentHour - 12 else currentHour

        val strCurrentEndAMPM = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        val strCurrentEndHour12 =
            if (currentHour + 1 > 12) currentHour + 1 - 12 else currentHour + 1

        currentTime = "$strCurrentAMPM $strCurrentHour12:$strCurrentMinute"
        currentEndTime = "$strCurrentEndAMPM $strCurrentEndHour12:$strCurrentMinute"

        // 시작 TimePicker 시간 변경 시
        binding.tpStart.setOnTimeChangedListener { _, newHour, newMinute ->
            val strNewAmPm: String = if (newHour >= 12) "오후" else "오전"
            val strNewHour = if (newHour == 0) 12 else if (newHour > 12) newHour - 12 else newHour
            val strNewMinute = if (newMinute / 10 == 0) "0$newMinute" else newMinute.toString()

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
            val strNewHour = if (newHour == 0) 12 else if (newHour > 12) newHour - 12 else newHour
            val strNewMinute = if (newMinute / 10 == 0) "0$newMinute" else newMinute.toString()

            currentEndTime = "$strNewAmPm $strNewHour:$strNewMinute"
            updateEndTimeText(
                currentEndYear,
                currentEndMonth,
                currentEndDay,
                currentEndWeek,
                currentEndTime
            )
        }
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
            .setTimeZone(SEOUL_TIME_ZONE_ID)

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
                        val calendar = Calendar.getInstance(SEOUL_TIME_ZONE)
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

                        updateStartTimeText(
                            currentStartYear,
                            currentStartMonth,
                            currentStartDay,
                            currentStartWeek,
                            currentTime
                        )

                        binding.npEndMonth.value = currentEndMonth
                        binding.npEndDay.value = currentEndDay

                        updateEndTimeText(
                            currentEndYear,
                            currentEndMonth,
                            currentEndDay,
                            currentEndWeek,
                            currentEndTime
                        )

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
                        val calendar = Calendar.getInstance(SEOUL_TIME_ZONE)
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

                        updateStartTimeText(
                            currentStartYear,
                            currentStartMonth,
                            currentStartDay,
                            currentStartWeek,
                            currentTime
                        )

                        binding.npEndMonth.value = currentEndMonth
                        binding.npEndDay.value = currentEndDay

                        updateEndTimeText(
                            currentEndYear,
                            currentEndMonth,
                            currentEndDay,
                            currentEndWeek,
                            currentEndTime
                        )

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

        var reminders = Event.Reminders()
        reminders.useDefault = false

        val reminderOptions = listOf(
            Triple(notificationBinding.tvBottomSheetNotificationNone, "알림 없음", null),
            Triple(notificationBinding.tvBottomSheetNotificationStart, "일정 시작시간", 0),
            Triple(notificationBinding.tvBottomSheetNotificationMinute, "10분 전", 10),
            Triple(notificationBinding.tvBottomSheetNotificationHour, "1시간 전", 60),
            Triple(notificationBinding.tvBottomSheetNotificationDay, "1일 전", 24 * 60)
        )

        reminderOptions.forEach { (textView, displayText, minutes) ->
            textView.setOnClickListener {
                binding.tvAlarm.text = displayText

                reminders = Event.Reminders()
                reminders.useDefault = minutes == null

                if (minutes != null) {
                    val reminder = EventReminder()
                    reminder.method = "popup"
                    reminder.minutes = minutes
                    reminders.overrides = listOf(reminder)
                }

                eventInfo.reminders = reminders

                dialog.dismiss()
            }
        }

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

        val repeatOptions = listOf(
            Triple(repeatBinding.tvBottomSheetRepeatNone, "반복 없음", null),
            Triple(repeatBinding.tvBottomSheetRepeatDay, "매일", "RRULE:FREQ=DAILY"),
            Triple(repeatBinding.tvBottomSheetRepeatWeek, "매주", "RRULE:FREQ=WEEKLY"),
            Triple(repeatBinding.tvBottomSheetRepeatMonth, "매월", "RRULE:FREQ=MONTHLY"),
            Triple(repeatBinding.tvBottomSheetRepeatYear, "매년", "RRULE:FREQ=YEARLY")
        )

        repeatOptions.forEach { (textView, displayText, recurrenceRule) ->
            textView.setOnClickListener {
                binding.tvRepeat.text = displayText
                eventInfo.recurrence = if (recurrenceRule != null) listOf(recurrenceRule) else null
                dialog.dismiss()
            }
        }

        repeatBinding.btnBottomSheetRepeatClose.setOnClickListener {
            dialog.dismiss()
        }
    }


    // 해당 월의 일 수 가져오기
    private fun getMaxDayOfMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance(SEOUL_TIME_ZONE)
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


    /**
     * 새로운 일정을 캘린더에 추가
     * @param event 추가할 일정 정보가 포함된 Event 객체
     */
    private fun addSchedule(event: Event) {
        calendarUtil.mID = 2  // 일정 추가
        calendarUtil.getResultsFromApi(null, event, null)
    }

}