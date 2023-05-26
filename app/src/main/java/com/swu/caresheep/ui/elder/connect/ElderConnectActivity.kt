package com.swu.caresheep.ui.elder.connect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityElderConnectBinding
import com.swu.caresheep.ui.start.user_id

class ElderConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityElderConnectBinding

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로가기 클릭 시 실행시킬 코드 입력
            finish()
            overridePendingTransition(R.anim.none, R.anim.slide_out_right)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElderConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        getUserCode()
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        // 뒤로 가기
        binding.ivClose.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
    }

    /**
     * 사용자 코드 가져오기
     */
    private fun getUserCode() {
        Firebase.database(BuildConfig.DB_URL)
            .getReference("Users")
            .orderByChild("id")
            .equalTo(user_id.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val userCode = data.child("code").getValue(String::class.java)
                            binding.tvUserCode.text = userCode
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 쿼리 실행 중 오류 발생 시 처리할 내용
                }
            })
    }

    /**
     * 연결된 보호자 가져오기
     */
    private fun getConnectedGuardian() {
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
                        }


                    } else {
                        // 해당 user_id를 가진 데이터가 Guardian 테이블에 존재하지 않는 경우

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 쿼리 실행 중 오류 발생 시 처리할 내용
                }
            })
    }
}