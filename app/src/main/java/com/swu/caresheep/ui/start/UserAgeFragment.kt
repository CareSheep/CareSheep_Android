package com.swu.caresheep.ui.start

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.FirebaseDatabase
import com.swu.caresheep.GuardianHelperClass
import com.swu.caresheep.UserHelperClass
import com.swu.caresheep.databinding.FragmentUserAgeBinding
import com.swu.caresheep.ui.guardian.GuardianActivity

class UserAgeFragment : Fragment() {

    private lateinit var binding: FragmentUserAgeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserAgeBinding.inflate(inflater, container, false)

        binding.etUserAge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.btnComplete.isEnabled = false
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.btnComplete.isEnabled = p0?.isNotEmpty() == true
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })


        // 완료 버튼 클릭 시
        binding.btnComplete.setOnClickListener {
            val args = requireArguments()
            val userType = args.getString("user_type")

//            Log.e(
//                "userType, userName, userGender, userAge",
//                userType.toString() + "/ " + userName.toString() + "/ " + userGender.toString() + "/ " + userAge.toString()
//            )

            // DB에 회원 정보 저장
            saveData()

            // 회원가입 완료 다이얼로그 표시


            // 전달 값에 따라 어르신 또는 보호자 홈 화면으로 이동
            if (userType == "elder") {
//                val intent = Intent(this.context, ???::class.java)
//                startActivity(intent)
            } else {
                val intent = Intent(this.context, GuardianActivity::class.java)
                startActivity(intent)
            }
        }

        return binding.root
    }

    private fun saveData() {
        // 저장할 데이터 받아오기
        val args = requireArguments()
        val userType = args.getString("user_type")
        val userName = args.getString("user_name")
        val userGender = args.getString("user_gender")
        val userAge = binding.etUserAge.text.toString().toInt()

        val gmail = activity?.intent?.extras?.getString("gmail")

        // 저장 경로 설정
        val pathNode = if (userType == "elder") "Users" else "Guardian"
        val rootNode =
            FirebaseDatabase.getInstance("https://caresheep-dcb96-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val reference = rootNode.getReference(pathNode)

        // 사용자 유형에 따라 저장할 객체 달라짐
        if (pathNode == "Users") {
            // id는 임시로 1, code는 임시로 123456 부여 -> 이후 랜덤 값으로 수정하기
            val userHelperClass =
                UserHelperClass(1, userGender, userName, userAge, 123456, gmail)

            val num = 0
            reference.push().setValue(userHelperClass)
                .addOnSuccessListener {
                    // 저장 성공 시
                    Log.e("회원가입 - 어르신", "회원정보 DB에 저장 성공")
                }
                .addOnFailureListener { e ->
                    // 저장 실패 시
                    Log.e("회원가입 - 어르신", "회원정보 DB에 저장 실패")
                }

        } else {
            // id는 임시로 1, code는 임시로 123456 부여 -> 이후 랜덤 값으로 수정하기
            val guardianHelperClass =
                GuardianHelperClass(1, null, userName, userAge, userGender, 123456, gmail)

            val num = 0
            reference.push().setValue(guardianHelperClass)
                .addOnSuccessListener {
                    // 저장 성공 시
                    Log.e("회원가입 - 보호자", "회원정보 DB에 저장 성공")
                }
                .addOnFailureListener { e ->
                    // 저장 실패 시
                    Log.e("회원가입 - 보호자", "회원정보 DB에 저장 실패")
                }
        }

//        reference.child("su").setValue(userHelperClass)


    }

}