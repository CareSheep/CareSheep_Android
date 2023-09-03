package com.swu.caresheep.guardian.calendar

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.gson.Gson
import com.swu.caresheep.R
import com.swu.caresheep.data.model.GuardianSchedule
import com.swu.caresheep.databinding.ActivityGuardianScheduleDetailBinding
import com.swu.caresheep.utils.calendar.CalendarUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class GuardianScheduleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianScheduleDetailBinding
    private lateinit var calendarUtil: CalendarUtil

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
            overridePendingTransition(R.anim.none, R.anim.fade_out)
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

        // 일정 세부 정보 설정
        setupScheduleDetails(item)

        // 뒤로 가기
        binding.ivBack.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        // 삭제 버튼
        binding.btnDelete.setOnClickListener {
            // 일정 삭제
            deleteSchedule(item.eventId)

            // 삭제 상태 저장을 위한 SharedPreferences 설정
            val sharedPrefs = getSharedPreferences("Delete Schedule", Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean("isDeleted", true).apply()

            onBackPressedCallback.handleOnBackPressed()
            Toast.makeText(this, "일정을 삭제했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.fade_in, R.anim.none)
    }

    /**
     * 일정 세부 정보를 UI에 설정
     * @param item GuardianSchedule (일정 정보 객체)
     */
    private fun setupScheduleDetails(item: GuardianSchedule) {
        // 일정 제목
        binding.etScheduleTitle.text = Editable.Factory.getInstance().newEditable(item.title)

        // 일정 유형 (시간 / 종일)
        binding.btnTime.isSelected = item.type == 0
        binding.btnAllDay.isSelected = item.type == 1

        // 일정 메모
        val memoText = item.memo ?: ""
        binding.etMemo.text = Editable.Factory.getInstance().newEditable(memoText)

        // 일정 시작 / 종료 시간
        val startTime = LocalDateTime.parse(item.startTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
        val endTime = LocalDateTime.parse(item.endTime.toString(), DateTimeFormatter.ISO_DATE_TIME)

        val outputFormat = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", Locale.KOREAN)
        val outputFormatWithTime = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E) a hh:mm", Locale.KOREAN)

        // 일정 시작 및 종료 시간 포멧팅
        val formattedStartDate = if (item.type == 1) {
            startTime.format(outputFormat)
        } else {
            startTime.format(outputFormatWithTime)
        }

        val formattedEndDate = if (item.type == 1) {
            endTime.format(outputFormat)
        } else {
            endTime.format(outputFormatWithTime)
        }

        binding.tvStartTime.text = formattedStartDate
        binding.tvEndTime.text = formattedEndDate

        binding.tvAlarm.text = item.notification  // 일정 알림
        binding.tvRepeat.text = item.repeat  // 일정 반복
    }

    /**
     * 주어진 eventId를 지닌 일정을 삭제
     * @param eventId 삭제할 일정의 eventId
     */
    private fun deleteSchedule(eventId: String) {
        calendarUtil.mID = 4  // 일정 삭제
        calendarUtil.getResultsFromApi(null, null, eventId)
    }

}