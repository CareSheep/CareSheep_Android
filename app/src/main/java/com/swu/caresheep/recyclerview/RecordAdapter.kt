package com.swu.caresheep.recyclerview

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.R
import com.swu.caresheep.Voice

class RecordAdapter(private val context: Context) :
RecyclerView.Adapter<RecordAdapter.ViewHolder>(){
    //var datas = mutableListOf<RecordData>()
    var datas = mutableListOf<Voice>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_recycler_record_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val list_number: TextView = itemView.findViewById(R.id.list_number)
        private val record_context: TextView = itemView.findViewById(R.id.record_context)
        private val record_date: TextView = itemView.findViewById(R.id.record_date)

        //fun bind(item: RecordData) {
        fun bind(item: Voice) {
            list_number.text = item.voice_id.toString()
            //record_context.text = item.content
            // item.content.length > 10이면 substring 메서드를 사용하여 10글자까지만 자르고 '...'을 붙임
            if (item.content.length > 10) {
                // "${item.content.substring(0, 10)}..."
                record_context.maxLines = 1
                record_context.ellipsize = TextUtils.TruncateAt.END
                record_context.text = item.content.substring(0, 10) + "..."

            } else {  //그렇지 않으면 그대로 item.content를 출력
                //item.content
                record_context.maxLines = Integer.MAX_VALUE
                record_context.ellipsize = null
                record_context.text = item.content
            }
            // record_context TextView의 layout_width 속성 값을 match_parent로 설정
            record_context.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT

            record_date.text = item.recording_date
        }
    }
}

