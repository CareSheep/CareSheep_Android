package com.swu.caresheep.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.R

class RecyclerRecordAdapter: RecyclerView.Adapter<ViewHolderRecord>() {

    private var modelList = ArrayList<RecycleRecordModel>()
    // 뷰홀더가 생성 됐을 때
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderRecord {
        // 연결할 레이아웃 설정
        return ViewHolderRecord(LayoutInflater.from(parent.context).inflate(R.layout.layout_recycler_record_item, parent, false))
    }

    // 목록의 아이템 개수
    override fun getItemCount(): Int {
        return this.modelList.size
    }

    // 뷰와 뷰홀더가 묶였을 때
    override fun onBindViewHolder(holder: ViewHolderRecord, position: Int) {
        holder.bind(this.modelList[position])
        // 클릭됐을 때
        holder.itemView.setOnClickListener {
            // 세부 내용 화면으로

        }
    }

    // 외부에서 데이터 넘기기
    fun submitList(modelList: ArrayList<RecycleRecordModel>) {
        this.modelList = modelList
    }

}