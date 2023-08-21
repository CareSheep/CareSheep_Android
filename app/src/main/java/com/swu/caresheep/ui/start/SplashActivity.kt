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
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivitySplashBinding
import com.swu.caresheep.ui.elder.main.ElderActivity
import com.swu.caresheep.ui.guardian.GuardianActivity

var user_id: Int = 0

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 설정
        val windowController = WindowInsetsControllerCompat(this.window, this.window.decorView)
        windowController.isAppearanceLightStatusBars = false

        // 로그인 상태를 확인하고 이동 처리
        checkLoginStatus()
    }

    /**
     * 로그인 상태 확인 후 적절한 화면 표시
     */
    private fun checkLoginStatus() {
        val account = GoogleSignIn.getLastSignedInAccount(this)

        if (account != null) {
            // 로그인된 경우 적절한 액티비티로 이동
            navigateToActivity(account.email.toString())
        } else {
            // 로그인되지 않은 경우 스플래시 화면 표시
            loadSplashScreen()
        }
    }

    /**
     * 로그인된 사용자의 이메일을 기반으로 액티비티 분기 처리
     * @param email 사용자 구글 이메일
     */
    private fun navigateToActivity(email: String) {
        val userRef = Firebase.database(DB_URL).getReference("Users")
        val guardianRef = Firebase.database(DB_URL).getReference("Guardian")

        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User 테이블에 해당 이메일 정보가 있는 경우 ElderActivity로 이동
                    for (data in snapshot.children) {
                        user_id = data.child("id").getValue(Int::class.java)!!
                    }
                    Handler(mainLooper).postDelayed({
                        val intent =
                            Intent(applicationContext, ElderActivity::class.java)
                        startActivity(intent)
                        finish()
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }, 2500)
                } else {
                    // User 테이블에 해당 이메일 정보가 없으면 Guardian 테이블을 검사
                    guardianRef.orderByChild("gmail").equalTo(email)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (data in snapshot.children) {
                                        val userId =
                                            data.child("user_id").getValue(Int::class.java)
                                        if (userId != null)
                                            user_id = userId
                                    }
                                    // Guardian 테이블에 해당 이메일 정보가 있는 경우 GuardianActivity로 이동
                                    Handler(mainLooper).postDelayed({
                                        val intent =
                                            Intent(applicationContext, GuardianActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                    }, 2500)
                                } else {
                                    // Guardian 테이블에 해당 이메일 정보가 없으면 스플래시 화면 표시
                                    loadSplashScreen()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // 데이터 조회 오류 처리
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 데이터 조회 오류 처리
            }
        }

        // User 테이블에서 이메일 정보 검사
        userRef.orderByChild("gmail").equalTo(email).addListenerForSingleValueEvent(userListener)
    }

    /**
     * 스플래시 화면 표시
     */
    private fun loadSplashScreen() {
        Handler(mainLooper).postDelayed({
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }, 2500)
    }

}