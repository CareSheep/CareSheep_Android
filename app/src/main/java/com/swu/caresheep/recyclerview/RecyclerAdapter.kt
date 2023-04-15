package com.swu.caresheep.recyclerview

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.GuardianVoiceRecordDetailFragment
import com.swu.caresheep.R

class RecyclerAdapter: RecyclerView.Adapter<ViewHolder>() {

    private var modelList = ArrayList<RecycleModel>()
    // 뷰홀더가 생성 됐을 때
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 연결할 레이아웃 설정
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_recycler_item, parent, false))
    }

    // 목록의 아이템 개수
    override fun getItemCount(): Int {
        return this.modelList.size
    }

    // 뷰와 뷰홀더가 묶였을 때
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(this.modelList[position])
        // 클릭됐을 때
        holder.itemView.setOnClickListener {
            // 세부 내용 화면으로

        }
    }

    // 외부에서 데이터 넘기기
    fun submitList(modelList: ArrayList<RecycleModel>) {
        this.modelList = modelList
    }

}