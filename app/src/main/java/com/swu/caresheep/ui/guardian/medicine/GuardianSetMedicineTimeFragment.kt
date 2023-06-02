package com.swu.caresheep.ui.guardian.medicine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.MedicineTime
import com.swu.caresheep.R
import com.swu.caresheep.elder.AlarmReceiver
import java.util.*

class GuardianSetMedicineTimeFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Firebase Realtime Database에서 데이터 가져오기
        val database =
            FirebaseDatabase.getInstance(DB_URL)
        val reference = database.getReference("MedicineTime")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (childSnapshot in dataSnapshot.children) {
                    val medicineData = childSnapshot.getValue(MedicineTime::class.java)
                    val info = arrayOf(
                        medicineData?.time,
                        medicineData?.medicine_name,
                        medicineData?.color,
                        medicineData?.user_id,
                        medicineData?.count,
                        medicineData?.single_dose
                    )

                    // time 필드 String으로 변환
                    val timeString = info[0].toString()

                    Log.d("Firebase", "시간 값: $timeString")

                    // 시간 : 분 형태로 돼있어서 split으로 분리시킨 후 int로 변환
                    val timeParts = timeString.split(":")
                    if (timeParts.size == 2) {
                        val hour = timeParts[0].toIntOrNull()
                        val minute = timeParts[1].toIntOrNull()
                        // 해당 시간에 알람 설정
                        if (hour != null && minute != null) {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)

                            // 현재 시간보다 이전이면 다음 날로 설정
                            if (calendar.before(Calendar.getInstance())) {
                                calendar.add(Calendar.DATE, 1)
                            }

                            val alarmIntent =
                                Intent(requireContext(), AlarmReceiver::class.java)
                            val alarmManager =
                                requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
                                    calendar.timeInMillis,
                                    alarmCallPendingIntent
                                )
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    alarmCallPendingIntent
                                )
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 가져오기가 실패한 경우 처리할 내용
                val exception = databaseError.toException()
                Log.e("FirebaseDatabase", "Failed to read value: ${exception.message}", exception)

            }
        })


        return inflater.inflate(R.layout.fragment_guardian_set_medicine_time, container, false)
    }
}