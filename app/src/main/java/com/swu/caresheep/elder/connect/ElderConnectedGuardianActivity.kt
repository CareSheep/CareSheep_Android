package com.swu.caresheep.elder.connect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.data.model.ConnectedGuardian
import com.swu.caresheep.databinding.ActivityElderConnectedGuardianBinding
import com.swu.caresheep.start.user_id

class ElderConnectedGuardianActivity : AppCompatActivity() {

    private lateinit var binding: ActivityElderConnectedGuardianBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElderConnectedGuardianBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getConnectedGuardian()
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        // 뒤로 가기
        binding.ivClose.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
    }


    /**
     * 연결된 보호자 가져오기
     */
    private fun getConnectedGuardian() {
        val connectedGuardianData = ArrayList<ConnectedGuardian>()
        var connectedGuardianRVAdapter: ElderConnectRVAdapter

        Firebase.database(BuildConfig.DB_URL)
            .getReference("Guardian")
            .orderByChild("user_id")
            .equalTo(user_id.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // 해당 user_id를 가진 데이터가 Guardian 테이블에 존재하는 경우
                        for (data in snapshot.children) {
                            val guardianName =
                                data.child("guardian_name").getValue(String::class.java)!!

                            connectedGuardianData.add(ConnectedGuardian(guardianName))
                        }

                        connectedGuardianRVAdapter = ElderConnectRVAdapter(connectedGuardianData)
                        binding.rvConnectedGuardian.adapter = connectedGuardianRVAdapter

                        binding.tvConnectedGuardianNotExist.visibility = View.INVISIBLE
                        binding.rvConnectedGuardian.visibility = View.VISIBLE
                    } else {
                        // 해당 user_id를 가진 데이터가 Guardian 테이블에 존재하지 않는 경우
                        binding.tvConnectedGuardianNotExist.visibility = View.VISIBLE
                        binding.rvConnectedGuardian.visibility = View.INVISIBLE

                        val context = applicationContext
                        val resources = context?.resources
                        val animation =
                            resources?.let { AnimationUtils.loadAnimation(context, R.anim.fade_in) }

                        animation?.also { hyperspaceJumpAnimation ->
                            binding.llConnectedGuardian.startAnimation(
                                hyperspaceJumpAnimation
                            )
                            binding.tvConnectedGuardianNotExist.startAnimation(
                                hyperspaceJumpAnimation
                            )
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 쿼리 실행 중 오류 발생 시 처리할 내용
                }
            })
    }
}