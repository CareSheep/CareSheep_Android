package com.swu.caresheep.guardian.routine.medicine

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.data.model.MedicineTime
import com.swu.caresheep.R
import android.content.Intent
import android.widget.TextView

class MedicineTimePickerListAdapter(private val context: Context)
    : RecyclerView.Adapter<MedicineTimePickerListAdapter.ViewHolder>() {

    var datas1 = mutableListOf<MedicineTime>()
    var timeString : String = ""

    lateinit var intent1 : Intent

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_recycler_timepicker_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas1.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas1[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val list_num: TextView = itemView.findViewById(R.id.list_num)
        private val time_context : TextView = itemView.findViewById(R.id.time_context)

        fun bind(item: MedicineTime){
            list_num.text = "1"
            time_context.text = item.time

            val time = intent1.getStringExtra("time")
            time_context.text = time
        }
    }
}
