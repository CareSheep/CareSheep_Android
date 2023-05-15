package com.swu.caresheep.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.MedicineTime
import com.swu.caresheep.R

abstract class MedicineTimePickerListAdapter(private val context: Context)
    : RecyclerView.Adapter<MedicineTimePickerListAdapter.ViewHolder>(){

    var datas = mutableListOf<MedicineTime>()
    var timeString : String = ""


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineTimePickerListAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_recycler_timepicker_item,parent,false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {}
}