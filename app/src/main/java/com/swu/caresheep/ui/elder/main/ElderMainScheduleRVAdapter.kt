package com.swu.caresheep.ui.elder.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.databinding.ItemElderScheduleBinding

class ElderMainScheduleRVAdapter(private var scheduleList: ArrayList<ElderMainSchedule>) :
    RecyclerView.Adapter<ElderMainScheduleRVAdapter.ViewHolder>() {

    interface MyItemClickListener {
        fun onItemClick(schedule: ElderMainSchedule)
    }

    private lateinit var mItemClickListener: MyItemClickListener
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: ItemElderScheduleBinding =
            ItemElderScheduleBinding.inflate(
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

    fun setData(items: ArrayList<ElderMainSchedule>) {
        this.scheduleList = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemElderScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ElderMainSchedule) {
            binding.tvScheduleTime.text = item.time
            binding.tvScheduleTitle.text = item.title
        }
    }

}
