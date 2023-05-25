package com.swu.caresheep.ui.guardian

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.swu.caresheep.R
import kotlinx.android.synthetic.clearFindViewByIdCache
import kotlinx.android.synthetic.main.fragment_guardian_elder_week_report.thissun
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter


class GuardianElderWeekReportFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_guardian_elder_week_report, container, false)
        val button1 = view.findViewById<Button>(R.id.testbutton)

        button1.setOnClickListener {
            getThisSunday()
        }

        return view
        // getThisSunday()

    }

    //val nextDate = dateToFind.plusDays(1)
    //val formattedNextDate = nextDate.format(dateFormat)
    private fun getThisSunday(){
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        val thisSunday = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY))
        val formattedThisSunday = thisSunday.format(dateFormat)

        thissun.setText(formattedThisSunday)

        println("Today: ${today.format(dateFormat)}")
        println("This Sunday: $formattedThisSunday")
        Log.d("Sunday",formattedThisSunday)
    }

}