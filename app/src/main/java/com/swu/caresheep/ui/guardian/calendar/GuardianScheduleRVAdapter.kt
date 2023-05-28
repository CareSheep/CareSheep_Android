package com.swu.caresheep.ui.guardian.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.databinding.ItemGuardianScheduleBinding

class GuardianScheduleRVAdapter(private var scheduleList: ArrayList<GuardianSchedule>) :
    RecyclerView.Adapter<GuardianScheduleRVAdapter.ViewHolder>() {

    interface MyItemClickListener {
        fun onItemClick(schedule: GuardianSchedule)
    }

    private lateinit var mItemClickListener: MyItemClickListener
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): GuardianScheduleRVAdapter.ViewHolder {
        val binding: ItemGuardianScheduleBinding =
            ItemGuardianScheduleBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(scheduleList[position])
        holder.itemView.setOnClickListener { mItemClickListener.onItemClick(scheduleList[position]) }
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }

    fun setData(list: ArrayList<GuardianSchedule>) {
        this.scheduleList.clear()
        list.addAll(list)
    }

    inner class ViewHolder(val binding: ItemGuardianScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GuardianSchedule) {
            "${item.startTime} - ${item.endTime}".also { binding.tvScheduleTime.text = it }
            binding.tvScheduleTitle.text = item.title
        }
    }
}