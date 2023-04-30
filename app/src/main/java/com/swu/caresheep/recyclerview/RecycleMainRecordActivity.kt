package com.swu.caresheep.recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_recycle_record_main.*

class RecycleMainRecordActivity : AppCompatActivity() {
    // 띄울 데이터들 모음(배열)
    var modelList = ArrayList<RecycleRecordModel>()

    // 리사이클러뷰에 연결할 어뎁터
    private lateinit var recyclerRecordAdapter: RecyclerRecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_record_main)


        // test용
        for (i in 1..10) {
            var recycleRecordModel =
                RecycleRecordModel(number = "$i", context = "$i 번째 내용입니다.", record_date = "2023/04/14")
            this.modelList.add(recycleRecordModel)
        }

        // 어뎁터 인스턴스 생성
        recyclerRecordAdapter = RecyclerRecordAdapter()
        recyclerRecordAdapter.submitList(this.modelList)
        // 리사이클러뷰 설정
        my_recycler_view.apply {
            //리사이클러뷰 방향 등 설정
            layoutManager =
                LinearLayoutManager(this@RecycleMainRecordActivity, LinearLayoutManager.VERTICAL, false)
            // 어뎁터 장착
            adapter = recyclerRecordAdapter

        }

    }
}