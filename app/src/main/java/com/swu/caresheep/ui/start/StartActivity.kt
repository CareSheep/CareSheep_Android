package com.swu.caresheep.ui.start

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig.DB_URL
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityStartBinding
import com.swu.caresheep.ui.elder.main.ElderActivity
import com.swu.caresheep.ui.guardian.GuardianActivity

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    // Google Sign-In Variables
    private lateinit var client: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        client = this.let { GoogleSignIn.getClient(this, gso) }

        binding.btnGoogleSignIn.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = client.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { data ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                    if (task.isSuccessful) {
                        val account = task.getResult(ApiException::class.java)!!
                        handleSignInResult(account)
                    } else {
                        Log.w("[START]", "Google sign in failed")
                    }
                }
            } else {
                Log.e("[START]", "Google Result Error $result")
            }
        }

    private fun handleSignInResult(account: GoogleSignInAccount) {
        try {
            // 이미 회원일 경우
            // gmail 가지고 와서 DB에서 어르신인지, 보호자인지 확인
            val gmail = account.email.toString()

            // User 테이블에서 이메일 주소를 키 값으로 가지는 노드가 존재하는지 확인
            Firebase.database(DB_URL)
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
                            Firebase.database(DB_URL)
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
                                            val intent = Intent(
                                                this@StartActivity,
                                                SignUpActivity::class.java
                                            )
                                            intent.putExtra("gmail", account.email)
                                            startActivity(intent)
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
        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }

    }
}