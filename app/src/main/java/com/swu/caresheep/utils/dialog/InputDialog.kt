package com.swu.caresheep.utils.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.swu.caresheep.databinding.DialogInputBinding

class InputDialog(context: AppCompatActivity) {

    private var binding: DialogInputBinding = DialogInputBinding.inflate(context.layoutInflater)
    private val dialog = Dialog(context)
    private lateinit var topBtnListener: TopBtnClickedListener
    private lateinit var bottomBtnListener: BottomBtnClickedListener

    init {
        dialog.setContentView(binding.root)
    }

    fun getDiseaseNameEditText(): EditText {
        return binding.etDiseaseName
    }

    fun show(title: String, caption: String, topBtnTitle: String, bottomBtnTitle: String) {
        //배경 색 날리기
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //창 크기
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        binding.tvVerticalTitle.text = title
        binding.tvVerticalCaption.text = caption
        binding.btnContinue.text = topBtnTitle
        binding.btnQuit.text = bottomBtnTitle

        dialog.show()

        binding.btnContinue.setOnClickListener {
            topBtnListener.onClicked(true)
            dialog.dismiss()
        }

        binding.btnQuit.setOnClickListener {
            bottomBtnListener.onClicked(true)
            dialog.dismiss()
        }
    }

    fun bottomBtnClickListener(listener: (Boolean) -> Unit) {
        this.bottomBtnListener = object : BottomBtnClickedListener {
            override fun onClicked(content: Boolean) {
                listener(content)
            }
        }
    }

    fun topBtnClickListener(listener: (Boolean) -> Unit) {
        this.topBtnListener = object : TopBtnClickedListener {
            override fun onClicked(content: Boolean) {
                listener(content)
            }
        }
    }

    interface TopBtnClickedListener {
        fun onClicked(content: Boolean)
    }

    interface BottomBtnClickedListener {
        fun onClicked(content: Boolean)
    }
}