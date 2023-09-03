package com.swu.caresheep.elder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.swu.caresheep.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.ui.elder.main.ElderActivity
import com.swu.caresheep.ui.start.user_id
import kotlinx.android.synthetic.main.activity_elder_walk.goal_walk
import kotlinx.android.synthetic.main.activity_elder_walk.walktimeTV
import java.time.LocalDate

class ElderWalkActivity : AppCompatActivity(), SensorEventListener {

    //현재 날짜
    private val todayDate = LocalDate.now().toString()

    private lateinit var dbRef: DatabaseReference
    private lateinit var dbRef2: DatabaseReference

    lateinit var sensorManager: SensorManager
    var stepCountSensor: Sensor? = null
    lateinit var stepCountView: TextView
    lateinit var stopButton: Button

    // 현재 걸음 수
    var currentSteps = 0

    // 목표 걸음 수
    var goalSteps = 0
    var goalWalk = 0

    // 결과
    var result1 = 0

    // 스탑워치
    var running: Boolean = false // 상태
    var pauseTime = 0L //멈춤 시간

    @RequiresApi(api = Build.VERSION_CODES.Q)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elder_walk)
        stepCountView = findViewById(R.id.stepCountView)
        stopButton = findViewById(R.id.stopButton)


        // 목표 걸음 수 가져오기
        getTodayWalkData()

        //화면 설정
        viewMode("stop")

        //정지 상태일 때만 실행
        if (!running) {
            //기본 셋팅
            walktimeTV.base = SystemClock.elapsedRealtime() - pauseTime

            //시작
            walktimeTV.start()

            //화면 설정
            viewMode("start")
        }

        // Check activity recognition permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 0)
        }

        // Connect step sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // Check if step sensor is available on the device
        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show()
        }

        // Add reset button - reset functionality
        stopButton.setOnClickListener {
            // Reset current step count
            //stepCountView.text = currentSteps.toString()
            //실행 상태일때만 실행
            todayWalkData()
            if (running) {
                //정지
                walktimeTV.stop()

                //정지 시간 저장
                pauseTime = SystemClock.elapsedRealtime() - walktimeTV.base

                //화면 설정
                viewMode("stop")
            }

            if(goalWalk <= currentSteps){
                val intent = Intent(this, ElderWalkDoneActivity::class.java)
                startActivity(intent)
            } else{
                val intent = Intent(this, ElderActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }



        }
    }

    //화면 설정
    private fun viewMode(mode: String) {

        //활성화 처리
        if (mode == "start") {
            //startBtn.isEnabled = false
            stopButton.isEnabled = true
            running = true
        } else {
            //startBtn.isEnabled = true
            stopButton.isEnabled = false
            running = false
        }
    }

    private fun getTodayWalkData() {
        FirebaseDatabase.getInstance(DB_URL).getReference("UsersRoutine").orderByChild("user_id")
            .equalTo(user_id.toDouble()).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            goalWalk =
                                data.child("walk_step").getValue(Int::class.java)!!

                            // 목표 걸음 수 설정
                            goal_walk.text = goalWalk.toString()
                            goalSteps = goalWalk
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Failed to read value.")
                }
            })
    }

    private fun todayWalkData() {

//        if(goalWalk_value1.toInt() <= currentSteps){
//            result1 = 1
//        }

        val data2 = hashMapOf(
            "done" to 1,
            "goal_walk" to goalWalk,
            "start_time" to todayDate,
            "walk" to currentSteps,
            "user_id" to user_id,
        )

        dbRef2 = FirebaseDatabase.getInstance(DB_URL).getReference("Walk")
        dbRef2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val childCount = dataSnapshot.childrenCount
                val walkid = (childCount + 1).toInt()
                // medicine_id = id // 약 고유번호 정해주기 -> 다음 액티비티에서 사용

                dbRef2.child(walkid.toString()).setValue(data2)
                    .addOnSuccessListener {
                        Log.e("걸음수 내역", "DB에 저장 성공")
                    }.addOnFailureListener {
                        Log.e("걸음수 내역", "DB에 저장 tlfvo")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("걸음수 내역", "Database error: $error")
            }
        })

    }

    override fun onStart() {
        super.onStart()
        stepCountSensor?.let {
            // Set sensor speed
            sensorManager
            sensorManager.registerListener(
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
                stepCountView.text = currentSteps.toString()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}