package com.swu.caresheep.ui.guardian.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.User
import com.swu.caresheep.databinding.ActivityGuardianConnectBinding
import com.swu.caresheep.ui.dialog.BaseDialog
import com.swu.caresheep.ui.dialog.VerticalDialog


class GuardianConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianConnectBinding
    private var code: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.slide_in_right, R.anim.none)

        binding.etUserCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.btnConnect.isEnabled = false
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.btnConnect.isEnabled = p0?.length == 6
                code = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })


        // 연결하기 버튼 클릭 시
        binding.btnConnect.setOnClickListener {
            // 사용자 정보 가져오기
            getUserInfo { userInfo ->
                // userInfo를 사용하여 필요한 작업을 수행하거나 반환하거나 처리할 수 있음

                if (userInfo.isEmpty()) {
                    // userInfo(사용자 정보)가 없으면
                    // 해당 사용자 코드를 가진 어르신 X 다이얼로그 표시
                    val baseDialog = BaseDialog(this)
                    baseDialog.show(getString(R.string.guardian_my_page_connect_not_exist_message))

                    baseDialog.btnClickListener {
                    }
                } else {
                    // userInfo(사용자 정보)가 있으면
                    // 어르신 연결 다이얼로그 표시
                    val verticalDialog = VerticalDialog(this)

                    verticalDialog.show(
                        userInfo,
                        getString(R.string.guardian_my_page_connect_elder_caption),
                        getString(R.string.connect),
                        getString(R.string.cancel)
                    )

                    verticalDialog.topBtnClickListener {
                        // 어르신 연결하기
                        connectElder(code)
                        this.onBackPressed()
                        Toast.makeText(this, "어르신 연결에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    }

                    verticalDialog.bottomBtnClickListener {
                    }
                }


            }

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.none, R.anim.slide_out_right)
    }

    /**
     * 해당 사용자 코드(code) 값을 가진 어르신 정보 반환
     **/
    private fun getUserInfo(callback: (String) -> Unit) {
        // 해당 code를 가진 어르신 불러오기
        Firebase.database(BuildConfig.DB_URL)
            .getReference("Users")
            .orderByChild("code")
            .equalTo(code)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var userInfo = ""
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val userName = data.child("username").getValue(String::class.java)
                            val userAge = data.child("age").getValue(Int::class.java)
                            var userGender = data.child("gender").getValue(String::class.java)

                            userGender = if (userGender == "female") "여" else "남"

                            userInfo = "$userName 어르신(${userAge}세/$userGender)"
                            Log.e("username", userInfo)
                        }
                    }
                    callback(userInfo)
                }

                override fun onCancelled(error: DatabaseError) {
                    // 쿼리 실행 중 오류 발생 시 처리할 내용
                    callback("")
                }
            })
    }

    /**
     *  보호자와 어르신 연동 (보호자 Table의 code 속성에 값 부여)
     **/
    private fun connectElder(code: String) {
        // 회원(Guardian)의 구글 계정 가져오기
        val account = this.let { GoogleSignIn.getLastSignedInAccount(this) }

        // gmail로 DB에서 사용자 정보 조회
        val gmail = account!!.email.toString()

        // Guardian 테이블에서 gmail을 키 값으로 가지는 노드 가져오기
        val database = FirebaseDatabase.getInstance(BuildConfig.DB_URL)
        val reference = database.getReference("Guardian")
        val query = reference.orderByChild("gmail").equalTo(gmail)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // 해당 Gmail을 가진 데이터가 Guardian 테이블에 존재하는 경우
                    for (data in snapshot.children) {
                        val guardianCode = data.key

                        // code 속성에 값을 부여
                        reference.child(guardianCode.toString()).child("code").setValue(code)
                            .addOnSuccessListener {
                                // 코드 값 부여 성공
                            }
                            .addOnFailureListener { error ->
                                // 코드 값 부여 실패
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 쿼리 실행 중 오류 발생 시 처리할 내용
                Log.e("GuardianConnectActivity", "Database query cancelled: ${error.message}")
            }
        })
    }


}