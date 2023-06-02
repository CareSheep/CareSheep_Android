package com.swu.caresheep.ui.start

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivitySignUpBinding
import com.swu.caresheep.ui.dialog.VerticalDialog

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var client: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        binding.ivBack.setOnClickListener {
            // 그만두기 다이얼로그 표시
            val verticalDialog = VerticalDialog(this)
            verticalDialog.show(
                getString(R.string.sign_up_quit_title),
                getString(R.string.sign_up_quit_caption),
                getString(R.string.sign_up_continue),
                getString(R.string.quit)
            )

            verticalDialog.topBtnClickListener {
                // 구글 로그아웃
                logout()
                finish()
            }

            verticalDialog.bottomBtnClickListener {
            }

        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
    }

    // 로그아웃
    private fun logout() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        client = this.let { GoogleSignIn.getClient(this, gso) }

        client.signOut()
            .addOnCompleteListener(this) {
                // 로그아웃 성공시 실행
                // 로그아웃 이후의 이벤트들(토스트 메세지, 화면 종료)을 여기서 수행하면 됨
            }
    }
}