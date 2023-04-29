package com.swu.caresheep.ui.start

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var splashScreen: SplashScreen
    private lateinit var binding: ActivityStartBinding

    // Google Sign-In Variables
    private lateinit var client: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen = installSplashScreen()
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Google Sign-In Methods
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()

        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.let { data ->
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                        if (task.isSuccessful) {
                            val account = task.getResult(ApiException::class.java)!!
                            firebaseAuthWithGoogle(account)
                        } else {
                            Log.w("START", "Google sign in failed")
                        }
                    }
                } else {
                    Log.e("START", "Google Result Error $result")
                }
            }

        binding.btnGoogleSignIn.setOnClickListener {
            val intent = client.signInIntent // 구글 로그인 페이지로 가는 인텐트 객체
            resultLauncher.launch(intent)
        }

    }

    // 로그아웃
    private fun firebaseAuthSignOut() {
        Firebase.auth.signOut()
    }

//    override fun onStart() {
//        super.onStart()
//        // DB에 있는 구글 계정일 시 회원가입 skip 구현하기
//        val account = this.let { GoogleSignIn.getLastSignedInAccount(it) }
//        if (account != null) {
//        }
//    }

    // Firebase에 사용자 인증 정보를 넘겨주는 부분 (구글 계정을 저장)
    // 전달받은 ID 토큰을 Firebase 사용자 인증 정보로 교환하고 해당 정보를 사용해 Firebase에 인증
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("START", "signInWithCredential:success")
                    val user = auth.currentUser

                    val intent = Intent(this, SignUpActivity::class.java)
                    intent.putExtra("gmail", account.email)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("START", "signInWithCredential:failure", task.exception)
                }
            }
    }
}