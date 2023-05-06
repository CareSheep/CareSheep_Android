package com.swu.caresheep.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.R

class ScheduleAdpater(private val context: Context) :
RecyclerView.Adapter<ScheduleAdpater.ViewHolder>() {
    var datas = mutableListOf<ScheduleData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_recycler_schedule_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val txt_date: TextView = itemView.findViewById(R.id.date)
        private val txt_schedule_item: TextView = itemView.findViewById(R.id.schedule_item1)
        private val txt_schedule_date: TextView = itemView.findViewById(R.id.schedule_date_1)

        fun bind(item: ScheduleData) {
            txt_date.text = item.date
            txt_schedule_item.text = item.schedule_item1.toString()
            txt_schedule_date.text = item.schedule_date1.toString()

        }
    }

}