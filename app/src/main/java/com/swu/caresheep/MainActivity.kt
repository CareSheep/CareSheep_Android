package com.swu.caresheep

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.swu.caresheep.ui.guardian.medicine.GuardianSetMedicineTimeFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var alarmCalendar: Calendar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        button2.setOnClickListener {
            val fragment_test = GuardianSetMedicineTimeFragment()
            val manager : FragmentManager = supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment_test).commit()

            alarmCalendar = Calendar.getInstance()
            alarmCalendar.timeInMillis = System.currentTimeMillis()


        }
    }
}