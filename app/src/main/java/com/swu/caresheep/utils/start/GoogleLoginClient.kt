package com.swu.caresheep.utils.start

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.data.model.User
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GoogleLoginClient {

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