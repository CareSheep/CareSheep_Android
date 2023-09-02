package com.swu.caresheep.ui.start

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.data.model.Guardian
import com.swu.caresheep.R
import com.swu.caresheep.data.model.User
import kotlin.random.Random
import com.swu.caresheep.databinding.FragmentUserAgeBinding
import com.swu.caresheep.ui.dialog.BaseDialog
import com.swu.caresheep.ui.elder.main.ElderActivity
import com.swu.caresheep.ui.guardian.GuardianActivity

class UserAgeFragment : Fragment() {

    private lateinit var binding: FragmentUserAgeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            val completeDialog = BaseDialog(activity as SignUpActivity)
            completeDialog.show(getString(R.string.sign_up_complete_message))

            completeDialog.btnClickListener {
                // 전달된 사융자 유형에 따라 어르신 또는 보호자 화면으로 이동
                if (userType == "elder") {
                    val intent = Intent(this.context, ElderActivity::class.java)
                    startActivity(intent)
                    this.activity?.finish()
                } else {
                    val intent = Intent(this.context, GuardianActivity::class.java)
                    startActivity(intent)
                    this.activity?.finish()
                }
            }

        }

        return binding.root
    }

    private fun saveData() {
        // 저장할 데이터 받아오기
        val args = requireArguments()
        val userType = args.getString("user_type")!!
        val userName = args.getString("user_name")!!
        val userGender = args.getString("user_gender")!!
        val userAge = binding.etUserAge.text.toString().toInt()

        val gmail = activity?.intent?.extras?.getString("gmail")!!
        var fcmtoken = ""

        // fcm 푸시 알림을 위한 FCM 토큰 받기
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            fcmtoken = task.result

        })

        // 저장 경로 설정
        val pathNode = if (userType == "elder") "Users" else "Guardian"
        val rootNode =
            FirebaseDatabase.getInstance(BuildConfig.DB_URL)
        val reference = rootNode.getReference(pathNode)

        // 사용자 유형에 따라 저장할 객체 달라짐
        if (pathNode == "Users") {

            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val childCount = dataSnapshot.childrenCount

                    val id = (childCount + 1).toInt() // User의 id (데이터 count number)

                    val user =
                        User(id, userGender, userName, userAge, getUserCode(reference), gmail)

                    reference.child(id.toString()).setValue(user)
                        .addOnSuccessListener {
                            // 저장 성공 시
                            user_id = id
                            Log.e("[회원가입] - 어르신", "회원정보 DB에 저장 성공")
                        }
                        .addOnFailureListener {
                            // 저장 실패 시
                            Log.e("[회원가입] - 어르신", "회원정보 DB에 저장 실패")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("[회원가입] - 어르신", "Database error: $error")
                }
            })
        } else {

            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val childCount = dataSnapshot.childrenCount

                    val id = (childCount + 1).toInt() // Guardian의 id (데이터 count number)

                    val guardian =
                        Guardian(id, null, userName, userAge, userGender, null, gmail, fcmtoken)

                    reference.child(id.toString()).setValue(guardian)
                        .addOnSuccessListener {
                            // 저장 성공 시
                            Log.e("[회원가입] - 보호자", "회원정보 DB에 저장 성공")
                        }
                        .addOnFailureListener {
                            // 저장 실패 시
                            Log.e("[회원가입] - 보호자", "회원정보 DB에 저장 실패")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("[회원가입] - 보호자", "Database error: $error")
                }
            })
        }

//        reference.push().setValue(userHelperClass)

    }


    // 어르신(User) code에 넣을 String(숫자 랜덤 6자리) 생성
    fun getUserCode(reference: DatabaseReference): String {
        val lowerBound = 0
        val upperBound = 999999
        val numDigits = 6

        var createdCode: String
        var existingCodes: List<String> = emptyList()
        val codeRef = reference.child("code")
        val codeListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                existingCodes = snapshot.children.mapNotNull { it.getValue(String::class.java) }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("[회원가입] - getUserCode 함수", "Failed to read value.", error.toException())
            }
        }
        codeRef.addListenerForSingleValueEvent(codeListener)

        do {
            // 랜덤 code 생성
            val num = Random.nextInt(upperBound + 1 - lowerBound) + lowerBound
            createdCode = num.toString().padStart(numDigits, '0')
        } while (existingCodes.contains(createdCode))

        return createdCode
    }

}