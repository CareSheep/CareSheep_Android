package com.swu.caresheep.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_recycler_item.view.*

// 커스텀 뷰홀더
class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    //layout_recycler_item 의 component(번호와 내용) 넣기
    private val listNumberTextView = itemView.list_number
    private val recordContextTextView = itemView.record_context
    private val recordDateTextView = itemView.record_date

    // 데이터와 뷰 묶기
    fun bind(recycleModel:RecycleModel) {
        //텍스트 뷰와 실제 텍스트 데이터 묶기
        listNumberTextView.text = recycleModel.number
        recordContextTextView.text = recycleModel.context
        recordDateTextView.text = recycleModel.record_date
    }
}