package com.swu.caresheep.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.swu.caresheep.databinding.DialogBaseBinding

class BaseDialog(private val context: AppCompatActivity) {

    private lateinit var binding: DialogBaseBinding
    private val dialog = Dialog(context)
    private lateinit var listener : CheckBtnClickedListener

    fun show(text: String) {
        binding = DialogBaseBinding.inflate(context.layoutInflater)
        dialog.setContentView(binding.root)

        // 배경 색 투명
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // 창 크기
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        dialog.show()

        binding.tvContent.text = text

        binding.btnCheck.setOnClickListener {
            listener.onClicked(true)
            dialog.dismiss()
        }
    }

    fun btnClickListener(listener: (Boolean) -> Unit) {
        this.listener = object: CheckBtnClickedListener {
            override fun onClicked(content: Boolean) {
                listener(content)
            }
        }
    }

    interface CheckBtnClickedListener {
        fun onClicked(content: Boolean)
    }
}