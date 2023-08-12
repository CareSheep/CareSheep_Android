package com.swu.caresheep.ui.guardian.calendar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.swu.caresheep.databinding.FragmentGuardianCalendarBinding
import com.swu.caresheep.utils.CalendarUtil
import com.swu.caresheep.utils.CalendarUtil.Companion.SEOUL_TIME_ZONE
import java.util.*

class GuardianCalendarFragment : Fragment() {

    private lateinit var binding: FragmentGuardianCalendarBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var selectedCalendar: Calendar
    private lateinit var calendarUtil: CalendarUtil

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = context.getSharedPreferences("SelectedDate", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuardianCalendarBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarUtil = CalendarUtil(requireContext(), this, null, binding)
        initView()
    }

    /**
     * 화면 초기화 및 설정
     */
    private fun initView() {
        // 오늘 날짜 표시
        selectedCalendar = Calendar.getInstance(SEOUL_TIME_ZONE)
        updateSelectedDateText()
        // 선택된 날짜 저장 (달력에서 선택된 날짜를 일정 추가 화면에 전달하기 위해)
        saveSelectedDate(selectedCalendar.time)

        calendarUtil.setupGoogleApi()
        calendarUtil.mID = 1  // 캘린더 생성
        calendarUtil.getResultsFromApi(selectedCalendar, null)

        // 달력에서 날짜를 선택했을 때의 동작
        binding.cvShared.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedCalendar.apply {
                set(year, month, dayOfMonth)
            }
            updateSelectedDateText()
            saveSelectedDate(selectedCalendar.time)
            calendarUtil.mID = 3  // 이벤트 불러오기
            calendarUtil.getResultsFromApi(selectedCalendar, null)
        }

        // 일정 추가 버튼
        binding.fabAddSchedule.setOnClickListener {
            val intent = Intent(requireContext(), GuardianAddScheduleActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * 선택된 날짜(일, 요일)를 UI에 표시
     */
    private fun updateSelectedDateText() {
        val dayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = selectedCalendar.getDisplayName(
            Calendar.DAY_OF_WEEK,
            Calendar.SHORT,
            Locale.getDefault()
        )
        val dateText = "${dayOfMonth}일 ($dayOfWeek)"
        binding.tvTodayDate.text = dateText
    }

    /**
     * 선택된 날짜를 저장
     * 일정 추가 화면으로 전달하기 위해 선택된 날짜를 SharedPreferences에 저장
     * @param selectedDate 저장할 선택된 날짜
     */
    private fun saveSelectedDate(selectedDate: Date) {
        val editor = sharedPreferences.edit()
        editor.putLong("selectedDate", selectedDate.time)
        editor.apply()
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
            // 2초 후에 API 결과 가져오기
            Handler(Looper.getMainLooper()).postDelayed({
                calendarUtil.getResultsFromApi(selectedCalendar, null)

                // isDeleted, isAdded 값 초기화
                sharedPrefsDelete.edit().putBoolean("isDeleted", false).apply()
                sharedPrefsAdd.edit().putBoolean("isAdded", false).apply()
            }, 2000)
        }
    }

}