package com.swu.caresheep.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.swu.caresheep.databinding.DialogQuitBinding

class QuitDialog(private val context: AppCompatActivity) {

    private lateinit var binding: DialogQuitBinding
    private val dialog = Dialog(context)
    private lateinit var listener : QuitBtnClickedListener

    fun show(title: String, caption: String) {
        binding = DialogQuitBinding.inflate(context.layoutInflater)
        dialog.setContentView(binding.root)

        //배경 색 날리기
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //창 크기
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        binding.tvQuitTitle.text = title
        binding.tvQuitCaption.text = caption

        dialog.show()

        binding.btnContinue.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnQuit.setOnClickListener {
            listener.onClicked(true)
            dialog.dismiss()
        }
    }

    fun btnClickListener(listener: (Boolean) -> Unit) {
        this.listener = object: QuitBtnClickedListener {
            override fun onClicked(content: Boolean) {
                listener(content)
            }
        }
    }

    interface QuitBtnClickedListener {
        fun onClicked(content: Boolean)
    }
}