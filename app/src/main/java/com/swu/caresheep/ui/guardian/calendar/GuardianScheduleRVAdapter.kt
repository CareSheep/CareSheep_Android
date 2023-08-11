package com.swu.caresheep.ui.guardian.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.databinding.ItemGuardianScheduleBinding
import com.swu.caresheep.utils.CalendarUtil.Companion.SEOUL_TIME_ZONE
import java.util.*
import kotlin.collections.ArrayList

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
            val calendar = Calendar.getInstance(SEOUL_TIME_ZONE)

            // 종일 유형
            if (item.type == 1)
                binding.tvScheduleTime.text = "하루 종일"
            else {
                // 시작 시간 계산
                val startTime: String

                val startTimeDate = Date(item.startTime.value)
                calendar.time = startTimeDate

                val startHour = calendar.get(Calendar.HOUR_OF_DAY)
                val startMinute = calendar.get(Calendar.MINUTE)
                val startAMPM = calendar.get(Calendar.AM_PM)

                val strStartMinute = if (startMinute / 10 == 0) "0$startMinute" else startMinute
                val strStartAMPM = if (startAMPM == Calendar.AM) "오전"
                else "오후"
                val startHour12 =
                    if (startHour == 0) 12 else if (startHour > 12) startHour - 12 else startHour

                startTime = "$strStartAMPM ${startHour12}:${strStartMinute}"


                // 일정 종료 시간 계산
                val endTime: String
                val endTimeDate = Date(item.endTime.value)
                calendar.time = endTimeDate

                val endHour = calendar.get(Calendar.HOUR_OF_DAY)
                val endMinute = calendar.get(Calendar.MINUTE)
                val endAMPM = calendar.get(Calendar.AM_PM)

                val strEndMinute = if (endMinute / 10 == 0) "0$endMinute" else endMinute
                val strEndAMPM = if (endAMPM == Calendar.AM) "오전"
                else "오후"
                val endHour12 =
                    if (endHour == 0) 12 else if (endHour > 12) endHour - 12 else endHour

                endTime = "$strEndAMPM ${endHour12}:${strEndMinute}"

                "$startTime - $endTime".also { binding.tvScheduleTime.text = it }
            }


            binding.tvScheduleTitle.text = item.title
        }
    }
}