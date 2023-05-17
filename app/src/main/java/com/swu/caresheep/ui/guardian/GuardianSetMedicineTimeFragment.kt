package com.swu.caresheep.ui.guardian

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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GuardianSetMedicineTimeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GuardianSetMedicineTimeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var alarmCalendar: Calendar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //시간 설정
        alarmCalendar = Calendar.getInstance()
        // 테스트 할 때 맞춰주세요
        alarmCalendar.set(Calendar.HOUR_OF_DAY, 1)
        alarmCalendar.set(Calendar.MINUTE, 24)
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GuardianSetMedicineTimeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GuardianSetMedicineTimeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}