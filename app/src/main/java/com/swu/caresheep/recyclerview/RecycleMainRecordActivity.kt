package com.swu.caresheep.recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_recycle_main_schedule.*
import kotlinx.android.synthetic.main.activity_recycle_record_main.*

class RecycleMainRecordActivity : AppCompatActivity() {

    lateinit var recordAdapter: RecordAdapter
    val datas = mutableListOf<RecordData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_record_main)

        initRecycler()
    }
    private fun initRecycler() {
        recordAdapter = RecordAdapter(this)
        rv_recorder.adapter = recordAdapter


        datas.apply {
            add(RecordData(recording_date = "4/5/2023", voice_id = 1, content = "오전8:00~오전9:00"))
            add(RecordData(recording_date = "4/6/2023", voice_id = 2, content = "오전10:00~오전11:00"))
            add(RecordData(recording_date = "4/7/2023", voice_id = 3, content = "오후3:00~오후5:00"))


            recordAdapter.datas = datas
            recordAdapter.notifyDataSetChanged()

        }
    }


}