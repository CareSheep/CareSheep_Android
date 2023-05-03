package com.swu.caresheep.recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_recycle_main_schedule.*

class RecycleMainScheduleActivity : AppCompatActivity() {

    lateinit var scheduleAdapter: ScheduleAdpater
    val datas = mutableListOf<ScheduleData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_main_schedule)

        initRecycler()
    }
    private fun initRecycler() {
        scheduleAdapter = ScheduleAdpater(this)
        rv_schedule.adapter = scheduleAdapter


        datas.apply {
            add(ScheduleData(date = "4/5/2023", schedule_item1 = "핑크힐병원가기", schedule_date1 = "오전8:00~오전9:00"))
            add(ScheduleData(date = "4/6/2023", schedule_item1 = "노란색약먹기", schedule_date1 = "오전10:00~오전11:00"))
            add(ScheduleData(date = "4/7/2023", schedule_item1 = "홍대병원가기", schedule_date1 = "오후3:00~오후5:00"))


            scheduleAdapter.datas = datas
            scheduleAdapter.notifyDataSetChanged()

        }
    }

}