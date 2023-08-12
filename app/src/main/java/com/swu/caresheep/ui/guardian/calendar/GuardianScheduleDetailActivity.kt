package com.swu.caresheep.ui.guardian.calendar

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.gson.Gson
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianScheduleDetailBinding
import com.swu.caresheep.utils.CalendarUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class GuardianScheduleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianScheduleDetailBinding
    private lateinit var calendarUtil: CalendarUtil

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
        calendarUtil = CalendarUtil(this, null, this, binding)

        calendarUtil.setupGoogleApi()

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
        calendarUtil.mID = 4  // 일정 삭제
        calendarUtil.getResultsFromApi(null, null, eventId)
    }

}