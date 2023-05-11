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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_elder_walk.stopButton
import kotlinx.android.synthetic.main.activity_elder_walk.walktimeTV
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_elder_walk.goal_walk
import kotlinx.android.synthetic.main.fragment_guardian_elder_today_report.lunch_check


class ElderWalkActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var dbRef: DatabaseReference

    var sensorManager: SensorManager? = null
    var stepCountSensor: Sensor? = null
    var stepCountView: TextView? = null
    //var resetButton: Button? = null
    private lateinit var serviceIntent: Intent
    private var timerStarted = false
    private var time = 0.0

    // 현재 걸음 수
    var currentSteps = 0

    // 목표 걸음 수
    var goalSteps = 0

    // 결과
    var result = 0

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

        // 프로그래스바
        findViewById<ComposeView>(R.id.composeView).setContent {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ){
                CircularProgressBar(percentage = 0.8f, number = 100)
            }
        }

        val stepCountView = findViewById<TextView>(R.id.stepCountView)
        //val resetButton = findViewById<Button>(R.id.resetButton)

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
            if(goalSteps < currentSteps){
                result = 1
            }

        })

        // 리셋 버튼 추가 - 리셋 기능
//        resetButton.setOnClickListener(View.OnClickListener { // 현재 걸음수 초기화
//            currentSteps = 0
//            stepCountView.setText(currentSteps.toString())
//        })
    }

    private fun getTodayWalkData() {
        dbRef = FirebaseDatabase.getInstance().getReference("Walk").child("test")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val goalWalk_value = snapshot.child("goal_walk").getValue().toString()
                    goal_walk.setText("$goalWalk_value")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })
    }

    public override fun onStart() {
        super.onStart()
        if (stepCountSensor != null) {
            // 센서 속도 설정
            // * 옵션
            // - SENSOR_DELAY_NORMAL: 20,000 초 딜레이
            // - SENSOR_DELAY_UI: 6,000 초 딜레이
            // - SENSOR_DELAY_GAME: 20,000 초 딜레이
            // - SENSOR_DELAY_FASTEST: 딜레이 없음
            //
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

    // 프로그래스바
    @Composable
    fun CircularProgressBar(
        percentage:Float,
        number:Int,
        fontSize: TextUnit = 28.sp,
        radius: Dp = 100.dp,
        color : Color = Color.Green,
        animDuration: Int = 1000,
        animDelay : Int = 0
    ){
        var animationPlayed by remember{
            mutableStateOf(false)
        }

        val Of = 0.0f
        val curPercentage = animateFloatAsState(
            targetValue = if(animationPlayed) percentage else Of,
            animationSpec = tween(
                durationMillis = animDuration,
                delayMillis = animDelay
            )
        )

        LaunchedEffect(key1 = true){
            animationPlayed = true
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(radius * 2f)
        ){
            Canvas(modifier = Modifier.size(radius*2f)){
                drawArc(
                    color = color,
                    -90f,
                    360 * curPercentage.value,
                    useCenter = false,
                    style = Stroke(cap = StrokeCap.Round, width = 25f)
                )
            }
            Text(
                text = (curPercentage.value*number).toInt().toString(),
                color = Color.Black,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}