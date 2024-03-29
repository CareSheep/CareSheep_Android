package com.swu.caresheep.guardian.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.databinding.FragmentGuardianMyPageBinding
import com.swu.caresheep.utils.dialog.VerticalDialog
import com.swu.caresheep.guardian.GuardianActivity
import com.swu.caresheep.guardian.emergency.GuardianElderEmergencyActivity
import com.swu.caresheep.start.StartActivity
import com.swu.caresheep.start.user_id

class GuardianMyPageFragment : Fragment() {

    private lateinit var binding: FragmentGuardianMyPageBinding
    private lateinit var client: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuardianMyPageBinding.inflate(inflater, container, false)

        // 어르신 루틴 정보 버튼 클릭 시
        binding.btnElderRoutineInfo.setOnClickListener {
            val intent = Intent(requireContext(), GuardianElderRoutineActivity::class.java)
            startActivity(intent)
        }

        // 어르신 연결하기 버튼 클릭 시
        binding.btnConnectElder.setOnClickListener {
            val intent = Intent(requireContext(), GuardianConnectActivity::class.java)
            startActivity(intent)
        }

        // 어르신 긴급 상황 전화 버튼 클릭 시
        binding.btnEmergencyElder.setOnClickListener {
            val intent = Intent(requireContext(), GuardianElderEmergencyActivity::class.java)
            startActivity(intent)
        }


        binding.btnDementiaElder.setOnClickListener {
            // 치매 어르신 설정 다이얼로그 표시
            val verticalDialog = VerticalDialog(context as GuardianActivity)

            verticalDialog.show(
                "어르신이 치매 환자이신가요?",
                "치매 환자를 위한 기능을 사용할 수 있도록 설정합니다",
                "치매 환자입니다",
                "아니요"
            )

            verticalDialog.topBtnClickListener {
                // 치매 어르신 설정하기
                setDementiaElder(1)
            }

            verticalDialog.bottomBtnClickListener {
                setDementiaElder(0)
            }
        }

        // 로그아웃 버튼 클릭 시
        binding.btnLogout.setOnClickListener {
            logout()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        getGuardianInfo()

        AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
            .also { hyperspaceJumpAnimation ->
                binding.tvUserName.startAnimation(hyperspaceJumpAnimation)
                binding.tvUserGmail.startAnimation(hyperspaceJumpAnimation)
                binding.ivEditUserName.startAnimation(hyperspaceJumpAnimation)
                binding.ivConnectInfo.startAnimation(hyperspaceJumpAnimation)
                binding.btnElderConnectedInfo.startAnimation(hyperspaceJumpAnimation)
            }
    }

    override fun onResume() {
        super.onResume()

        getGuardianInfo()

        AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
            .also { hyperspaceJumpAnimation ->
                binding.tvUserName.startAnimation(hyperspaceJumpAnimation)
                binding.tvUserGmail.startAnimation(hyperspaceJumpAnimation)
                binding.ivEditUserName.startAnimation(hyperspaceJumpAnimation)
                binding.ivConnectInfo.startAnimation(hyperspaceJumpAnimation)
                binding.btnElderConnectedInfo.startAnimation(hyperspaceJumpAnimation)
            }
    }

    /**
     *  치매 어르신 설정
     **/
    private fun setDementiaElder(isDementia: Int) {
        val database = FirebaseDatabase.getInstance(BuildConfig.DB_URL)
        val reference = database.getReference("Dementia")

        reference.orderByChild("user_id").equalTo(user_id.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var foundRecord = false

                    for (data in snapshot.children) {
                        val recordId = data.key
                        val recordRef = reference.child(recordId!!)
                        recordRef.child("dementia").setValue(isDementia)
                            .addOnSuccessListener {
                                // 업데이트 성공 시
                            }
                            .addOnFailureListener {
                                // 업데이트 실패 시
                            }

                        foundRecord = true
                    }

                    if (!foundRecord) {
                        val childCount = snapshot.childrenCount
                        val newRecordId = (childCount + 1).toString()  // 튜플 개수 + 1을 사용하여 새로운 이름 생성

                        val newRecordRef = reference.child(newRecordId)
                        newRecordRef.child("user_id").setValue(user_id)
                        newRecordRef.child("dementia").setValue(isDementia)
                            .addOnSuccessListener {
                                // 추가 성공 시
                            }
                            .addOnFailureListener {
                                // 추가 실패 시
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 쿼리 실행 중 오류 발생 시 처리할 내용
                }
            })
    }

    /**
     * 로그아웃
     */
    private fun logout() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        client = this.let { GoogleSignIn.getClient(requireContext(), gso) }

        client.signOut()
            .addOnCompleteListener(requireActivity()) {
                // 로그아웃 성공시 실행
                // 로그아웃 이후의 이벤트들(토스트 메세지, 화면 종료)을 여기서 수행하면 됨
                val intent = Intent(requireContext(), StartActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                Toast.makeText(requireContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
    }

    /**
     * 사용자 (Guardian) 정보 표시
     */
    private fun getGuardianInfo() {
        try {
            // 회원(Guardian)의 구글 계정 가져오기
            val account = this.let { GoogleSignIn.getLastSignedInAccount(requireContext()) }

            // gmail로 DB에서 사용자 정보 조회
            val gmail = account!!.email.toString()

            // Guardian 테이블에서 gmail을 키 값으로 가지는 노드가 존재하는지 확인
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Guardian")
                .orderByChild("gmail")
                .equalTo(gmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // 해당 gmail을 가진 데이터가 Guardian 테이블에 존재하는 경우
                            for (data in snapshot.children) {
                                val guardianName =
                                    data.child("guardian_name").getValue(String::class.java)
                                // 사용자 (Guardian) 정보를 화면에 표시
                                "${guardianName}님".also { binding.tvUserName.text = it }
                                binding.tvUserGmail.text = gmail

                                // 어르신 연결 정보 표시
                                val code = data.child("code").getValue(String::class.java)

                                if (code == null) {
                                    binding.btnElderConnectedInfo.text = "어르신 연결 안 됨"
                                    binding.ivConnectInfo.setImageResource(R.drawable.ic_connect_info)
                                } else {
                                    Firebase.database(BuildConfig.DB_URL)
                                        .getReference("Users")
                                        .orderByChild("code")
                                        .equalTo(code.toString())
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    // 해당 코드 값을 가진 데이터가 Users 테이블에 존재하는 경우
                                                    for (data in snapshot.children) {
                                                        val userName =
                                                            data.child("username")
                                                                .getValue(String::class.java)
                                                        val userAge =
                                                            data.child("age")
                                                                .getValue(Int::class.java)
                                                        var userGender =
                                                            data.child("gender")
                                                                .getValue(String::class.java)

                                                        userGender = if (userGender == "female") "여"
                                                        else "남"

                                                        (userName + " 어르신 (" + userAge + "세/" + userGender + ")").also {
                                                            binding.btnElderConnectedInfo.text = it
                                                        }
                                                        binding.ivConnectInfo.setImageResource(R.drawable.ic_connect_info_on)
                                                    }

                                                } else {
                                                    // 해당 코드 값을 가진 데이터가 Users 테이블에 존재하지 않는 경우
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                // 쿼리 실행 중 오류 발생 시 처리할 내용
                                            }
                                        })
                                }
                            }
                        } else {
                            // 해당 gmail을 가진 데이터가 Guardian 테이블에 존재하지 않는 경우

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[GuardianMyPage] failed", "signInResult:failed code=" + e.statusCode)
        }
    }
}