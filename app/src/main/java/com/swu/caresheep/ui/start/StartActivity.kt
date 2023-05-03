package com.swu.caresheep.ui.start

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.swu.caresheep.databinding.ActivityStartBinding

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
            val intent = Intent(this, SignUpActivity::class.java)
            intent.putExtra("gmail", account.email)
            startActivity(intent)

        } catch (e: ApiException) {
            Log.w("[START] failed", "signInResult:failed code=" + e.statusCode)
        }

    }
}