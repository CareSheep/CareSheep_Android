package com.swu.caresheep.elder

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.swu.caresheep.R
import kotlinx.android.synthetic.main.activity_elder_medicine_first.*
import java.util.*


class ElderMedicineFirstActivity : AppCompatActivity() {
    private var flag = true
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }



        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_elder_medicine_first)

        calendar = Calendar.getInstance()


//        Thread {
//            while (flag == true) {
//                try {
//                    calendar = Calendar.getInstance()
//                    if (calendar.get(Calendar.HOUR_OF_DAY) > 0 && calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                        medicine_alarm.text = "AM ${calendar.get(Calendar.HOUR_OF_DAY)}시 ${calendar.get(Calendar.MINUTE)}분 ${calendar.get(Calendar.SECOND)}초"
//                    } else if (calendar.get(Calendar.HOUR_OF_DAY) == 12) {
//                        medicine_alarm.text = "PM ${calendar.get(Calendar.HOUR_OF_DAY)}시 ${calendar.get(Calendar.MINUTE)}분 ${calendar.get(Calendar.SECOND)}초"
//                    } else if (calendar.get(Calendar.HOUR_OF_DAY) > 12 && calendar.get(Calendar.HOUR_OF_DAY) < 24) {
//                        medicine_alarm.text = "PM ${(calendar.get(Calendar.HOUR_OF_DAY) - 12)}시 ${calendar.get(Calendar.MINUTE)}분 ${calendar.get(Calendar.SECOND)}초"
//                    } else if (calendar.get(Calendar.HOUR_OF_DAY) == 0) {
//                        medicine_alarm.text = "AM 0시 ${calendar.get(Calendar.MINUTE)}분 ${calendar.get(Calendar.SECOND)}초"
//                    }
//                    Thread.sleep(1000)
//                } catch (ex: InterruptedException) {
//                }
//            }
//        }.start()


     // 라디오버튼
        medicine.setOnCheckedChangeListener { radioGroup, checkedId ->
            when(checkedId) {
                // 약 먹음 체크
                R.id.medicine_done ->  {
                    finish()
                    flag = false
                }

//                // 약 먹지 않음 체크
//                R.id.medicine_no ->


            }
        }

    }
}