package com.swu.caresheep

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.swu.caresheep.elder.ElderVoiceSubActivity
import com.swu.caresheep.ui.guardian.GuardianSetMedicineTimeFragment
import com.swu.caresheep.ui.start.SignUpActivity
import kotlinx.android.synthetic.main.activity_elder_voice_main.*
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