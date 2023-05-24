package com.swu.caresheep.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.Guardian
import com.swu.caresheep.User
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GoogleLoginClient {

    private lateinit var guardian: Guardian

    /**
     * 보호자(Guardian)로 로그인한 경우
     * 보호자(Guardian) 객체 반환
     */
    fun getGuardianInfo(context: Context): Guardian {
        try {
            // 보호자(Guardian)의 구글 계정 가져오기
            val account = this.let { GoogleSignIn.getLastSignedInAccount(context) }

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
                                val guardianId =
                                    data.child("guardian_id")
                                        .getValue(Int::class.java)!!
                                val guardianUserId =
                                    data.child("user_id")
                                        .getValue(Int::class.java)!!
                                val guardianName =
                                    data.child("guardian_name")
                                        .getValue(String::class.java)!!
                                val guardianAge =
                                    data.child("age")
                                        .getValue(Int::class.java)!!
                                var guardianGender =
                                    data.child("gender")
                                        .getValue(String::class.java)
                                val guardianCode =
                                    data.child("code")
                                        .getValue(String::class.java)
                                val guardianGmail =
                                    data.child("gmail")
                                        .getValue(String::class.java)!!

                                guardianGender = if (guardianGender == "female") "여"
                                else "남"

                                guardian = Guardian(
                                    guardianId,
                                    guardianUserId,
                                    guardianName,
                                    guardianAge,
                                    guardianGender,
                                    guardianCode,
                                    guardianGmail
                                )
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // 쿼리 실행 중 오류 발생 시 처리할 내용
                    }
                })
        } catch (e: ApiException) {
            Log.w("[Guardian] failed", "signInResult:failed code=" + e.statusCode)
        }
        return guardian
    }

    /**
     * 보호자(Guardian)로 로그인한 경우
     * 연결된 어르신 (User) 반환
     */
    suspend fun getElderInfo(context: Context): User {
        val account = this.let { GoogleSignIn.getLastSignedInAccount(context) }
        val gmail = account?.email.toString()

        return suspendCoroutine { continuation ->
            val guardianRef = Firebase.database(BuildConfig.DB_URL)
                .getReference("Guardian")
                .orderByChild("gmail")
                .equalTo(gmail)

            val userRef = Firebase.database(BuildConfig.DB_URL)
                .getReference("Users")

            guardianRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val userCode = data.child("code").getValue(String::class.java)
                            if (userCode == null) {
                                continuation.resume(User(null, null, null, null, null, null))
                            } else {
                                userRef.orderByChild("code").equalTo(userCode)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(userDataSnapshot: DataSnapshot) {
                                            if (userDataSnapshot.exists()) {
                                                for (userData in userDataSnapshot.children) {
                                                    val userId =
                                                        userData.child("id")
                                                            .getValue(Int::class.java)!!
                                                    val userName =
                                                        userData.child("username")
                                                            .getValue(String::class.java)!!
                                                    val userAge =
                                                        userData.child("age")
                                                            .getValue(Int::class.java)!!
                                                    var userGender =
                                                        userData.child("gender")
                                                            .getValue(String::class.java)
                                                    val userGmail =
                                                        userData.child("gmail")
                                                            .getValue(String::class.java)!!

                                                    userGender =
                                                        if (userGender == "female") "여" else "남"

                                                    val connectedUser = User(
                                                        userId,
                                                        userGender,
                                                        userName,
                                                        userAge,
                                                        userCode,
                                                        userGmail
                                                    )

                                                    Log.e("connectedUser", connectedUser.gmail.toString())

                                                    continuation.resume(connectedUser)
                                                    return
                                                }
                                            }
                                            continuation.resume(
                                                User(
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null
                                                )
                                            )
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            continuation.resume(
                                                User(
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null
                                                )
                                            )
                                        }
                                    })
                            }
                        }
                    } else {
                        continuation.resume(User(null, null, null, null, null, null))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }
}