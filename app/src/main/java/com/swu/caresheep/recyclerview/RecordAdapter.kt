package com.swu.caresheep.recyclerview

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.R
import com.swu.caresheep.Voice
import com.swu.caresheep.ui.guardian.GuardianVoiceDetailActivity
import java.text.SimpleDateFormat
import java.util.*

class RecordAdapter(private val context: Context) :
    RecyclerView.Adapter<RecordAdapter.ViewHolder>(){
    var datas = mutableListOf<Voice>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_recycler_record_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val list_number: TextView = itemView.findViewById(R.id.list_number)
        private val record_context: TextView = itemView.findViewById(R.id.record_context)
        private val record_date: TextView = itemView.findViewById(R.id.record_date)
        private val circleIndicator: TextView = itemView.findViewById(R.id.circleIndicator)

        fun bind(item: Voice) {
            //list_number.text = item.voice_id.toString()
            // item.content.length > 10이면 substring 메서드를 사용하여 10글자까지만 자르고 '...'을 붙임
            if (item.content.length > 10) {
                // "${item.content.substring(0, 10)}..."
                record_context.maxLines = 1
                record_context.ellipsize = TextUtils.TruncateAt.END
                record_context.text = item.content.substring(0, 10) + "..."

            } else {  //그렇지 않으면 그대로 item.content를 출력
                record_context.maxLines = Integer.MAX_VALUE
                record_context.ellipsize = null
                record_context.text = item.content
            }
            // record_context TextView의 layout_width 속성 값을 match_parent로 설정
            record_context.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT

            // String 타입의 recording_date 를 -> Date 객체로
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val recordingDate = dateFormat.parse(item.recording_date)

            // 년 월 일 오전/오후 시:분:초 형식으로 포맷
            val formattedDate = SimpleDateFormat("yyyy년 MM월 dd일 a hh:mm:ss", Locale.getDefault())
                .format(recordingDate)

            record_date.text = formattedDate

            // 상황 여부에 따른 버튼 색상
            // 위험 상황일 경우
            if(item.danger == "1") { // 위험 상황일 경우
                val newColor: Int = ContextCompat.getColor(context, R.color.red)
                list_number.backgroundTintList = ColorStateList.valueOf(newColor) // 버튼 배경 색상을 빨간색으로
            }
            else if(item.in_need == "1")  {   // 물건 필요 상황일 경우
                val newColor: Int = ContextCompat.getColor(context, R.color.blue)
                list_number.backgroundTintList = ColorStateList.valueOf(newColor) // 버튼 배경 색상을 파랑색으로
            }
            else if(item.danger == "0" && item.in_need == "0"){
                val newColor: Int = ContextCompat.getColor(context, R.color.green)
                list_number.backgroundTintList = ColorStateList.valueOf(newColor)
            }

            // 읽음 여부
            if (item.check) { // 읽었으면
                circleIndicator.visibility = View.GONE
            } else if (item.check) { // 안 읽었으면
                circleIndicator.visibility = View.VISIBLE
            }

            // 클릭이벤트 ; 세부 내용 화면으로 전환
            itemView.setOnClickListener {
                item.check = true  // 읽음으로 바꾸기
                notifyItemChanged(adapterPosition) //

                Intent(context, GuardianVoiceDetailActivity::class.java).apply {
                    // Voice 필드의 데이터 자체를 전달
                    putExtra("check", item.check)
                    putExtra("content", item.content)
                    putExtra("danger", item.danger)
                    putExtra("recording_date", item.recording_date)
                    putExtra("in_need", item.in_need)
                    putExtra("user_id", item.user_id)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
            }

        }
    }

}