package com.swu.caresheep.elder

import android.hardware.SensorEventListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swu.caresheep.R
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Build
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_elder_walk.stopButton
import kotlinx.android.synthetic.main.activity_elder_walk.walktimeTV
import kotlin.math.roundToInt
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.ui.elder.main.ElderActivity
import kotlinx.android.synthetic.main.activity_elder_walk.goal_walk
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.lunch_check

class ElderWalkActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var dbRef: DatabaseReference

    lateinit var sensorManager: SensorManager
    var stepCountSensor: Sensor? = null
    lateinit var stepCountView: TextView
    //var resetButton: Button? = null

    private lateinit var serviceIntent: Intent
    private var timerStarted = false
    private var time = 0.0

    // 현재 걸음 수
    var currentSteps = 0

    // 목표 걸음 수
    var goalSteps = 0
    var goalWalk_value1 = 0

    // 결과
    var result1 = 0

    val walk = hashMapOf(
        "done" to "Los Angeles",
        "goal_walk" to "CA",
        "start_time" to "USA",
        "user_id" to "",
        "walk" to ""
    )
    @RequiresApi(api = Build.VERSION_CODES.Q)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elder_walk)

        stepCountView = findViewById(R.id.stepCountView)

        serviceIntent = Intent(applicationContext, ElderWalkTimerService::class.java)
        registerReceiver(updateTime, IntentFilter(ElderWalkTimerService.TIMER_UPDATED))

        //목표 걸음 수 가져오기
        getTodayWalkData()

        // 스탑와치 시작
        startTimer()

        // 활동 퍼미션 체크
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 0)
        }

        // 걸음 센서 연결
        // * 옵션
        // - TYPE_STEP_DETECTOR:  리턴 값이 무조건 1, 앱이 종료되면 다시 0부터 시작
        // - TYPE_STEP_COUNTER : 앱 종료와 관계없이 계속 기존의 값을 가지고 있다가 1씩 증가한 값을 리턴
        //
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCountSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // 디바이스에 걸음 센서의 존재 여부 체크
        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show()
        }

        stopButton.setOnClickListener(View.OnClickListener{
            stopTimer()
            pushTodayWalkData()
            if(goalSteps < currentSteps){
                result1 = 1
            }

            // 홈 화면으로 이동
            val intent = Intent(this, ElderActivity::class.java)
            startActivity(intent)

        })

        // 리셋 버튼 추가 - 리셋 기능
//        resetButton.setOnClickListener(View.OnClickListener { // 현재 걸음수 초기화
//            currentSteps = 0
//            stepCountView.setText(currentSteps.toString())
//        })
    }

    private fun getTodayWalkData() {
        dbRef = FirebaseDatabase.getInstance().getReference("UsersRoutine").child("test")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    goalWalk_value1 = snapshot.child("walk_step").getValue().toString() as Int
                    goal_walk.setText("$goalWalk_value1")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })
    }

    private fun pushTodayWalkData() {

        val data = hashMapOf(
            "done" to result1,
            "goal_walk" to goalWalk_value1,
            "start_time" to "",
            "walk" to currentSteps,
            "user_id" to 1,
        )
        dbRef = FirebaseDatabase.getInstance().getReference("Walk").child("test")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val goalWalk_value2:Int = snapshot.child("goal_walk").getValue() as Int
                    if(goalWalk_value2 <= currentSteps){
                        result1 = 1
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })
        dbRef.setValue(data)
    }

    public override fun onStart() {
        super.onStart()
        stepCountSensor?.let {
            // Set sensor speed
            sensorManager
            sensorManager!!.registerListener(
                this,
                stepCountSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        // 걸음 센서 이벤트 발생시
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                // 센서 이벤트가 발생할때 마다 걸음수 증가
                currentSteps++
                stepCountView!!.text = currentSteps.toString()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    //타이머
    private fun startTimer()
    {
        serviceIntent.putExtra(ElderWalkTimerService.TIME_EXTRA, time)
        startService(serviceIntent)
        timerStarted = true
    }

    private fun stopTimer()
    {
        stopService(serviceIntent)
        timerStarted = false
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            time = intent.getDoubleExtra(ElderWalkTimerService.TIME_EXTRA, 0.0)
            walktimeTV.text = getTimeStringFromDouble(time)
        }
    }

    private fun getTimeStringFromDouble(time: Double): String
    {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String = String.format("%02d:%02d:%02d", hour, min, sec)
}