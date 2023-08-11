package com.swu.caresheep.ui.guardian.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.GuardianMapsActivity
import com.swu.caresheep.R
import com.swu.caresheep.databinding.FragmentGuardianHomeBinding
import com.swu.caresheep.recyclerview.RecycleMainRecordActivity
import com.swu.caresheep.ui.guardian.GuardianElderReportActivity
import com.swu.caresheep.ui.guardian.emergency.AlarmReceiverEmergency
import com.swu.caresheep.ui.guardian.mypage.GuardianConnectActivity
import com.swu.caresheep.ui.start.user_id
import com.swu.caresheep.utils.CalendarUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class GuardianHomeFragment : Fragment() {

    private lateinit var binding: FragmentGuardianHomeBinding
    private lateinit var calendarUtil: CalendarUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        getEmergencyAlarm()
        binding = FragmentGuardianHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarUtil = CalendarUtil(requireContext(), this, binding)

        // 당겨서 새로고침 기능 세팅
        binding.layoutSwipeRefresh.setOnRefreshListener {
            updateTodaySchedule()
            binding.layoutSwipeRefresh.isRefreshing = false
        }

        // 스크롤 업 대신에 리프레쉬 이벤트가 트리거 되는걸 방지하기 위해서
        // scroll이 최상단에 위치했을 때만 refreshLayout을 활성화
        binding.layoutScroll.viewTreeObserver.addOnScrollChangedListener {
            binding.layoutSwipeRefresh.isEnabled = (binding.layoutScroll.scrollY == 0)
        }

        binding.clAnalysisResult.setOnClickListener {
            // 리포트 확인 화면으로 이동
            val intent = Intent(requireContext(), GuardianElderReportActivity::class.java)
            startActivity(intent)
        }

        binding.clVoiceMailbox.setOnClickListener {
            // 음성 사서함 목록 화면으로 이동
            val intent = Intent(requireContext(), RecycleMainRecordActivity::class.java)
            startActivity(intent)
        }

        binding.ivMap.setOnClickListener {
            // 위치 추적 지도 화면으로 이동
            val intent = Intent(requireContext(), GuardianMapsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (user_id == 0) {
            Toast.makeText(
                requireContext(),
                "사용자 코드를 입력하여 어르신과 연결하세요.",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(requireContext(), GuardianConnectActivity::class.java))
        } else {
            updateTodaySchedule()
            checkDementiaStatus()
        }
    }

    /**
     * 오늘의 일정 업데이트
     */
    private fun updateTodaySchedule() {
        val today = Calendar.getInstance()

        calendarUtil.setupGoogleApi()
        calendarUtil.mID = 3  // 이벤트 불러오기

        calendarUtil.getResultsFromApi(today)
    }

    /**
     * 치매 어르신 여부에 따른 지도 표시
     */
    private fun checkDementiaStatus() {
        val database = FirebaseDatabase.getInstance(DB_URL)
        val reference = database.getReference("Dementia")

        reference.orderByChild("user_id").equalTo(user_id.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val dementiaValue = data.child("dementia").getValue(Int::class.java)
                        if (dementiaValue == 1) {
                            // 치매 어르신인 경우
                            binding.ivMap.visibility = View.VISIBLE
                            val fadeInAnimation =
                                AnimationUtils.loadAnimation(context, R.anim.fade_in)
                            binding.ivMap.startAnimation(fadeInAnimation)
                        } else {
                            binding.ivMap.visibility = View.INVISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 쿼리 실행 중 오류 발생 시 처리할 내용
                }
            })
    }


    private fun getEmergencyAlarm() {
        try {
            val user_id = 1 // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Emergency")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val emergencyValue =
                                    data.child("emergency").getValue(Int::class.java)
                                if (emergencyValue == 1) {
                                    val emergencyTime =
                                        data.child("today_date").getValue(String::class.java)
                                            .toString()
                                    val dateTimeFormatter =
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                    val dateTime =
                                        LocalDateTime.parse(emergencyTime, dateTimeFormatter)
                                    val time = dateTime.toLocalTime().toString()

                                    val timeParts = time.split(":")
                                    if (timeParts.size == 2) {
                                        val hour = timeParts[0].toIntOrNull()
                                        val minute = timeParts[1].toIntOrNull()
                                        // 해당 시간에 알람 설정
                                        if (hour != null && minute != null) {
                                            val calendar = Calendar.getInstance()
                                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                                            calendar.set(Calendar.MINUTE, minute)
                                            calendar.set(Calendar.SECOND, 0)

                                            // 현재 시간보다 이전이면 다음 날로 설정하기
                                            if (calendar.before(Calendar.getInstance())) {
                                                calendar.add(Calendar.DATE, 1)
                                            }

                                            val alarmIntent = Intent(
                                                requireContext(),
                                                AlarmReceiverEmergency::class.java
                                            )
                                            val alarmManager =
                                                requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                            alarmIntent.action =
                                                AlarmReceiverEmergency.ACTION_RESTART_SERVICE
                                            val alarmCallPendingIntent = PendingIntent.getBroadcast(
                                                requireContext(),
                                                0,
                                                alarmIntent,
                                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }
    }
}