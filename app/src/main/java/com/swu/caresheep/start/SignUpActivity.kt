package com.swu.caresheep.start

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.swu.caresheep.R
import com.swu.caresheep.databinding.ActivitySignUpBinding
import com.swu.caresheep.utils.dialog.VerticalDialog

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var client: GoogleSignInClient
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private fun setBackBtnHandling() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로 가기를 눌렀을 때 그만두기 Dialog 호출
                showQuitDialog()
            }
        }
        onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        setBackBtnHandling()

        binding.ivBack.setOnClickListener {
            // 그만두기 다이얼로그 표시
            showQuitDialog()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
    }

    /**
     * 그만두기 Dialog 표시
     */
    private fun showQuitDialog() {
        val verticalDialog = VerticalDialog(this)
        verticalDialog.show(
            getString(R.string.sign_up_quit_title),
            getString(R.string.sign_up_quit_caption),
            getString(R.string.sign_up_continue),
            getString(R.string.quit)
        )

        verticalDialog.topBtnClickListener {
            // 로그아웃
            logout()
            finish()
        }

        verticalDialog.bottomBtnClickListener {
        }
    }

    /**
     * 로그아웃
     */
    private fun logout() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        client = this.let { GoogleSignIn.getClient(this, gso) }

        client.signOut()
            .addOnCompleteListener(this) {
                // 로그아웃 성공 시 실행
                Snackbar.make(binding.root, "로그아웃되었습니다.", Snackbar.LENGTH_SHORT).show()
            }
    }

}