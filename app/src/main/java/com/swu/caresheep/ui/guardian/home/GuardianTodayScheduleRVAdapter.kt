package com.swu.caresheep.ui.guardian.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.databinding.ItemGuardianTodayScheduleBinding
import com.swu.caresheep.ui.elder.main.ElderMainSchedule
import com.swu.caresheep.ui.guardian.calendar.GuardianSchedule

class GuardianTodayScheduleRVAdapter(private var todayScheduleList: ArrayList<GuardianSchedule>) :
    RecyclerView.Adapter<GuardianTodayScheduleRVAdapter.ViewHolder>() {

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
    ): GuardianTodayScheduleRVAdapter.ViewHolder {
        val binding: ItemGuardianTodayScheduleBinding =
            ItemGuardianTodayScheduleBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(todayScheduleList[position])
    }

    override fun getItemCount(): Int {
        return todayScheduleList.size
    }

//    fun setData(items: ArrayList<GuardianSchedule>) {
//        this.scheduleList = items
//        notifyDataSetChanged()
//    }

    fun setData(list: ArrayList<GuardianSchedule>) {
        this.todayScheduleList.clear()
        list.addAll(list)
    }

    inner class ViewHolder(val binding: ItemGuardianTodayScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GuardianSchedule) {
            "${item.startTime} - ${item.endTime}".also { binding.tvScheduleTime.text = it }
            binding.tvScheduleTitle.text = item.title
        }
    }
}