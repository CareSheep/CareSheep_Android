package com.swu.caresheep.guardian.report

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class GuardianEldereReportViewAdapter (fm: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fm, lifecycle){

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> GuardianElderTodayReportFragment()
            1 -> GuardianElderWeekReportFragment()
            else -> GuardianElderTodayReportFragment()
        }
    }

}