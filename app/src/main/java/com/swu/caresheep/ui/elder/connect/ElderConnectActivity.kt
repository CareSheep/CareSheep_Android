package com.swu.caresheep.ui.elder.connect

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityElderConnectBinding
import com.swu.caresheep.ui.start.user_id

class ElderConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityElderConnectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElderConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getUserCode()
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        // 뒤로 가기
        binding.ivClose.setOnClickListener {
            finish()
        }

        // 연결된 보호자 목록
        binding.btnConnectedGuardianList.setOnClickListener {
            val intent = Intent(this, ElderConnectedGuardianActivity::class.java)
            startActivity(intent)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
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

                            try {
                                val barcodeEncoder = BarcodeEncoder()
                                val bitmap = barcodeEncoder.encodeBitmap(userCode, BarcodeFormat.QR_CODE, 500, 500)
                                val drawable = BitmapDrawable(resources, bitmap)

                                binding.ivQrUserCode.setImageDrawable(drawable)

                                // 애니메이션 적용
                                val fadeInAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
                                binding.ivQrUserCode.startAnimation(fadeInAnimation)
                            } catch (e: Exception) {
                                Log.e("qr코드 경고", e.toString())
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 쿼리 실행 중 오류 발생 시 처리할 내용
                }
            })
    }

}