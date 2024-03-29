package com.swu.caresheep.guardian.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityGuardianElderRoutineBinding
import com.swu.caresheep.start.user_id
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_black_medicine_count_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_black_medicine_time_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_blue_medicine_count_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_blue_medicine_time_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_breakfast_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_brown_medicine_count_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_brown_medicine_time_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_dinner_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_green_medicine_count_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_green_medicine_time_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_lunch_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_purple_medicine_count_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_purple_medicine_time_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_red_medicine_count_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_red_medicine_time_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_walk_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_yellow_medicine_count_goal
import kotlinx.android.synthetic.main.activity_guardian_elder_routine.tv_yellow_medicine_time_goal

class GuardianElderRoutineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianElderRoutineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianElderRoutineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        getRoutine()
        getMedicine()

        // 뒤로 가기
        binding.ivBack.setOnClickListener {
            finish()
        }

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
    }

    private fun getRoutine() {
        try {
            val user_id = user_id // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("UsersRoutine")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val walkStepValue =
                                    data.child("walk_step").getValue(Int::class.java)
                                "${walkStepValue.toString()}보".also { tv_walk_goal.text = it }
                                val breakfastValue =
                                    data.child("breakfast").getValue(String::class.java)
                                tv_breakfast_goal.text = "$breakfastValue"
                                val lunchValue = data.child("lunch").getValue(String::class.java)
                                tv_lunch_goal.text = "$lunchValue"
                                val dinnerValue = data.child("dinner").getValue(String::class.java)
                                tv_dinner_goal.text = "$dinnerValue"
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun getMedicine() {
        try {
            val user_id = user_id // user_id로 수정
            Firebase.database(BuildConfig.DB_URL)
                .getReference("MedicineTime")
                .orderByChild("user_id")
                .equalTo(user_id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val color = data.child("color").getValue(String::class.java)
                                val name = data.child("medicine_name").getValue(String::class.java)
                                val count = data.child("count").getValue(Int::class.java)
                                if (color == "red") {
                                    tv_red_medicine_time_goal.text = "$name"
                                    tv_red_medicine_count_goal.text = "$count"
                                } else if (color == "yellow") {
                                    tv_yellow_medicine_time_goal.text = "$name"
                                    tv_yellow_medicine_count_goal.text = "$count"
                                } else if (color == "green") {
                                    tv_green_medicine_time_goal.text = "$name"
                                    tv_green_medicine_count_goal.text = "$count"
                                } else if (color == "blue") {
                                    tv_blue_medicine_time_goal.text = "$name"
                                    tv_blue_medicine_count_goal.text = "$count"
                                } else if (color == "purple") {
                                    tv_purple_medicine_time_goal.text = "$name"
                                    tv_purple_medicine_count_goal.text = "$count"
                                } else if (color == "brown") {
                                    tv_brown_medicine_time_goal.text = "$name"
                                    tv_brown_medicine_count_goal.text = "$count"
                                } else if (color == "black") {
                                    tv_black_medicine_time_goal.text = "$name"
                                    tv_black_medicine_count_goal.text = "$count"
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }
    }
}