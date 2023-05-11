package com.swu.caresheep.ui.guardian

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.swu.caresheep.R
import android.widget.TimePicker;
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_guardian_set_breakfast_time.timePicker3

class GuardianSetBreakfastTimeFragment : Fragment() {

    private lateinit var picker: TimePicker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guardian_set_breakfast_time, container, false)

        var mDBReference = FirebaseDatabase.getInstance().reference

        // 버튼 누르면 해당 타임피커의 시간은 사용자 루틴 테이블에 들어감
        picker = timePicker3
        picker.is24HourView = true

        val hour: Int
        val minute: Int
        val am_pm: String

        if (Build.VERSION.SDK_INT >= 23) {
            hour = picker.hour
            minute = picker.minute
        } else {
            hour = picker.currentHour
            minute = picker.currentMinute
        }

        if (hour > 12) {
            am_pm = "PM"
            hour -= 12
        } else {
            am_pm = "AM"
        }
    }


}