package com.swu.caresheep.ui.guardian.medicine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.swu.caresheep.R
import com.swu.caresheep.elder.AlarmReceiver
import java.util.*

class GuardianSetMedicineTimeFragment : Fragment() {


    private lateinit var alarmCalendar: Calendar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //시간 설정
        alarmCalendar = Calendar.getInstance()
        // 테스트 할 때 맞춰주세요
        alarmCalendar.set(Calendar.HOUR_OF_DAY, 2)
        alarmCalendar.set(Calendar.MINUTE, 26)
        alarmCalendar.set(Calendar.SECOND, 0)


        if (alarmCalendar.before(Calendar.getInstance())) {
            alarmCalendar.add(Calendar.DATE, 1)
        }

        val alarmIntent = Intent(requireContext(), AlarmReceiver::class.java)
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent.action = AlarmReceiver.ACTION_RESTART_SERVICE
        val alarmCallPendingIntent = PendingIntent.getBroadcast(
            requireActivity(),
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmCalendar.timeInMillis,
                alarmCallPendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmCalendar.timeInMillis,
                alarmCallPendingIntent
            )
        }


        return inflater.inflate(R.layout.fragment_guardian_set_medicine_time, container, false)
    }
}