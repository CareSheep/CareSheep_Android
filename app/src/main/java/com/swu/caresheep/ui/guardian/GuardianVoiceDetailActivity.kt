package com.swu.caresheep.ui.guardian

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.swu.caresheep.BuildConfig
import com.swu.caresheep.R
import com.swu.caresheep.Voice
import com.swu.caresheep.databinding.ActivityGuardianVoiceDetailBinding
import kotlinx.android.synthetic.main.activity_guardian_voice_detail.*
import java.text.SimpleDateFormat
import java.util.*

class GuardianVoiceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianVoiceDetailBinding
    lateinit var data: Voice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianVoiceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // RecordAdapter.kt에서 putExtra 메서드로 전달한 데이터 받기 & null일 경우 default 설정
        val content = intent.getStringExtra("content")
        val danger = intent.getStringExtra("danger")
        val recording_date = intent.getStringExtra("recording_date")
        val in_need = intent.getStringExtra("in_need")

        // String 타입의 recording_date 를 ->Date 객체로
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val recordingDate = recording_date?.let { dateFormat.parse(it) }

        // 년 월 일 오전/오후 시:분:초 형식으로 포맷
        val formattedDate = SimpleDateFormat("yyyy년 MM월 dd일 a hh:mm:ss", Locale.getDefault())
            .format(recordingDate)


        // 받은 데이터 사용하기
        binding.tvDateContent.text = formattedDate
        binding.tvRecordContent.text = content

        if (danger == "1") { // 위험 상황일 경우
            // 음성 내용 배경 색 변경 - 빨강
            val newColor: Int = ContextCompat.getColor(this, R.color.voice_red)
            binding.tvRecordContent.backgroundTintList = ColorStateList.valueOf(newColor)

            // 상황 유형 텍스트 
            binding.tvSituationTypeContent.text = "긴급"

            // 상황 유형 아이콘
            binding.ivSituationImage.setImageResource(R.drawable.ic_situation_type_emergency)

            // 하단 이미지
            binding.ivIllust.setImageResource(R.drawable.img_emergency_illust)
            binding.ivIllust.setOnClickListener {
                val intent1 = Intent(Intent.ACTION_DIAL)
                if(intent1.resolveActivity(packageManager) != null){
                    startActivity(intent1)
                }
            }
        } else if (in_need == "1") { // 생필품 부족 상황일 경우
            // 음성 내용 배경 색 변경 - 파랑
            val newColor: Int = ContextCompat.getColor(this, R.color.voice_blue)
            binding.tvRecordContent.backgroundTintList = ColorStateList.valueOf(newColor)

            // 상황 유형 텍스트 
            binding.tvSituationTypeContent.text = "생필품 부족"

            // 상황 유형 아이콘
            binding.ivSituationImage.setImageResource(R.drawable.ic_situation_type_need)

            // 하단 이미지
            binding.ivIllust.setImageResource(R.drawable.img_need_illust)

        } else if (in_need == "0" && danger == "0") { // 일상적인 상황일 경우
            // 음성 내용 배경 색 변경 - 초록
            val newColor: Int = ContextCompat.getColor(this, R.color.voice_green)
            binding.tvRecordContent.backgroundTintList = ColorStateList.valueOf(newColor)

            // 상황 유형 텍스트 
            binding.tvSituationTypeContent.text = "일상"

            // 상황 유형 아이콘
            binding.ivSituationImage.setImageResource(R.drawable.ic_situation_type_daily)

            // 하단 이미지
            binding.ivIllust.setImageResource(R.drawable.img_daily_illust)
        }



        // 환자 이름 정보 얻기
        getGuardianInfo()

        // 툴바의 닫기 아이콘 클릭 시 뒤로 가기
        binding.ivBack.setOnClickListener {
            finish()
        }

    }

    // 현재 로그인한 사용자와 연결된 어르신 찾기 (GuardianMyPageFragment 메서드 이용)
    private fun getGuardianInfo() {
        try {
            // 회원(Guardian)의 구글 계정 가져오기
            val account = GoogleSignIn.getLastSignedInAccount(this)

            // gmail로 DB에서 사용자 정보 조회
            val gmail = account?.email.toString()

            // gmail 이용해서 Guardian 테이블 확인
            Firebase.database(BuildConfig.DB_URL)
                .getReference("Guardian")
                .orderByChild("gmail")
                .equalTo(gmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // user_id 이용해서 username 찾기
                            for (data in snapshot.children) {
                                val user_id = data.child("user_id").getValue(Int::class.java)
                                val userRef = Firebase.database(BuildConfig.DB_URL)
                                    .getReference("Users")
                                    .child(user_id.toString())

                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        if (userSnapshot.exists()) {
                                            val username = userSnapshot.child("username")
                                                .getValue(String::class.java)

                                            // 어르신 성함
                                            "${username}님".also { tv_profile_name.text = it }
                                            Log.e("Username", "$username")

                                        }
                                    }

                                    override fun onCancelled(userError: DatabaseError) {
                                        //db 에러
                                    }
                                })
                            }
                        } else {
                            // Handle case where no Guardian entry is found for the user
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // db 에러
                    }
                })
        } catch (e: ApiException) {
            Log.w("[GuardianMyPage] failed", "signInResult:failed code=" + e.statusCode)
        }
    }
}



