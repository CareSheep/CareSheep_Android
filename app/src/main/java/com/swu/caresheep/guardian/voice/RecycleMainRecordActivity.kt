package com.swu.caresheep.guardian.voice

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import com.swu.caresheep.data.model.Voice
import kotlinx.android.synthetic.main.activity_recycle_record_main.*
import kotlinx.android.synthetic.main.activity_recycle_record_main.iv_close

class RecycleMainRecordActivity : AppCompatActivity() {

    lateinit var recordAdapter: RecordAdapter
    private val database = FirebaseDatabase.getInstance(DB_URL).getReference("Voice")  //Firebase DB의 Voice 테이블에 접근
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("read_status", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_record_main)

        // 툴바의 닫기 아이콘 클릭 쉬 뒤로가기
        iv_close.setOnClickListener {
            onBackPressed()
        }

        initRecycler()
        loadData()
    }

    private fun initRecycler() {// 리사이클러뷰와 어뎁터 초기화
        recordAdapter = RecordAdapter(this)
        rv_recorder.adapter = recordAdapter

    }
    private fun loadData() { //  Firebase Realtime Database에서 데이터를 가져와서 리사이클러뷰에 표시
        // 해당 경로의 데이터가 변경될 때마다 onDataChange() 콜백을 호출하여 데이터를 가져옴
        database.addValueEventListener(object : ValueEventListener {
            // 가져온 데이터를 RecordData 객체로 변환해서 recordList에 추가
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 리사이클러뷰에 표시할 데이터 리스트를 저장
                val recordList = mutableListOf<Voice>()

                for (snapshot in dataSnapshot.children.reversed()) { // db 저장된 역순(최신 것이 상위)
                    val record = snapshot.getValue(Voice::class.java)
                    record?.let {
                        it.check = sharedPreferences.getBoolean(snapshot.key, false)
                        recordList.add(it)
                    }
                }
                recordAdapter.datas = recordList
                recordAdapter.notifyDataSetChanged() // 리사이클러뷰 화면 갱신
            }

            override fun onCancelled(error: DatabaseError) {
                // 읽기 실패시 -> 예외 확인
                val exception = error.toException()
                Log.e("FirebaseDatabase", "Failed to read value: ${exception.message}", exception)

            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        //  읽기 상태를 sharedPreferences에 저장
        for (record in recordAdapter.datas) {
            sharedPreferences.edit().putBoolean(record.recording_date, record.check).apply()
        }
    }

}