package com.swu.caresheep.elder.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.data.model.ElderTodaySchedule
import com.swu.caresheep.databinding.ItemElderTodayScheduleBinding

class ElderTodayScheduleRVAdapter(private var scheduleList: ArrayList<ElderTodaySchedule>) :
    RecyclerView.Adapter<ElderTodayScheduleRVAdapter.ViewHolder>() {

    interface MyItemClickListener {
        fun onItemClick(schedule: ElderTodaySchedule)
    }

    private lateinit var mItemClickListener: MyItemClickListener
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: ItemElderTodayScheduleBinding =
            ItemElderTodayScheduleBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(scheduleList[position])
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }

    fun setData(items: ArrayList<ElderTodaySchedule>) {
        this.scheduleList = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemElderTodayScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ElderTodaySchedule) {
            if (item.type == 1)
                binding.tvScheduleTime.text = "하루 종일"
            else
                binding.tvScheduleTime.text = item.time

            binding.tvScheduleTitle.text = item.title
        }
    }

}
