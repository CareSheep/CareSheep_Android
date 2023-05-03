package com.swu.caresheep.ui.start

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivitySplashBinding
import com.swu.caresheep.ui.elder.main.ElderActivity
import com.swu.caresheep.ui.guardian.GuardianActivity

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 설정
        val windowController = WindowInsetsControllerCompat(this.window, this.window.decorView)
        windowController.isAppearanceLightStatusBars = false
    }

    private fun loadSplashScreen() {
        Handler().postDelayed({
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }, 3000)
    }

    override fun onStart() {
        super.onStart()
        val account = this.let { GoogleSignIn.getLastSignedInAccount(it) }
        if (account != null) {
            // 이미 로그인한 사용자임.
            // gmail 가지고 와서 DB에서 어르신인지, 보호자인지 확인
            val gmail = account.email.toString()

            // User 테이블에서 이메일 주소를 키 값으로 가지는 노드가 존재하는지 확인
            Firebase.database("https://caresheep-dcb96-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users")
                .orderByChild("gmail")
                .equalTo(gmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // 해당 이메일 주소를 가진 데이터가 User 테이블에 존재하는 경우
                            Handler().postDelayed({
                                val intent = Intent(applicationContext, ElderActivity::class.java)
                                startActivity(intent)
                                finish()
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                            }, 3000)
                        } else {
                            // 해당 이메일 주소를 가진 데이터가 User 테이블에 존재하지 않는 경우

                            // Guardian 테이블에서 이메일 주소를 키 값으로 가지는 노드가 존재하는지 확인
                            Firebase.database("https://caresheep-dcb96-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .getReference("Guardian")
                                .orderByChild("gmail")
                                .equalTo(gmail)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            // 해당 이메일 주소를 가진 데이터가 Guardian 테이블에 존재하는 경우
                                            Handler().postDelayed({
                                                val intent = Intent(
                                                    applicationContext,
                                                    GuardianActivity::class.java
                                                )
                                                startActivity(intent)
                                                finish()
                                                overridePendingTransition(
                                                    R.anim.fade_in,
                                                    R.anim.fade_out
                                                )
                                            }, 3000)
                                        } else {
                                            // 해당 이메일 주소를 가진 데이터가 Guardian 테이블에 존재하지 않는 경우
                                            loadSplashScreen()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } else {
            loadSplashScreen()
        }
    }
}