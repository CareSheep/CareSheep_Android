package com.swu.caresheep.ui.elder.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.databinding.ActivityElderBinding
import com.swu.caresheep.elder.ElderVoiceMainActivity
import com.swu.caresheep.elder.ElderWalkActivity

class ElderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityElderBinding
    private var todayScheduleData = ArrayList<ElderMainSchedule>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 데이터 리스트 생성 더미 데이터
        todayScheduleData.apply {
            add(
                ElderMainSchedule(
                    "오후 4시",
                    "어린이집 가기"
                )
            )
            add(
                ElderMainSchedule(
                    "오후 5시",
                    "병원"
                )
            )
            add(
                ElderMainSchedule(
                    "오후 6시",
                    "장보기"
                )
            )
        }

        // 오늘의 일정 RecyclerView 어댑터와 데이터 리스트 연결
        val todayScheduleRVAdapter = ElderMainScheduleRVAdapter(todayScheduleData)
        todayScheduleRVAdapter.setData(todayScheduleData)
        binding.rvTodaySchedule.adapter = todayScheduleRVAdapter

        // 음성 메시지 전송 버튼
        binding.btnVoiceRecord.setOnClickListener {
            val intent = Intent(this, ElderVoiceMainActivity::class.java)
            startActivity(intent)
        }

        // 만보기 측정 버튼
        binding.btnWalk.setOnClickListener {
            val intent = Intent(this, ElderWalkActivity::class.java)
            startActivity(intent)
        }
    }
}