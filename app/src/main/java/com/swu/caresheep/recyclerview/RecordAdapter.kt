package com.swu.caresheep.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.R

class RecordAdapter(private val context: Context) :
RecyclerView.Adapter<RecordAdapter.ViewHolder>(){
    var datas = mutableListOf<RecordData>()
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

        fun bind(item: RecordData) {
            list_number.text = item.voice_id.toString()
            record_context.text = item.content
            record_date.text = item.recording_date
        }
    }
}

