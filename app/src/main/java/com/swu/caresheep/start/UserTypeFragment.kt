package com.swu.caresheep.start

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.swu.caresheep.R
import com.swu.caresheep.databinding.FragmentUserTypeBinding
import com.swu.caresheep.start.UserTypeFragmentDirections

class UserTypeFragment : Fragment() {

    private lateinit var binding: FragmentUserTypeBinding
    private var isElderClicked: Boolean = false
    private var isGuardianClicked: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserTypeBinding.inflate(inflater, container, false)

        // 어르신 버튼 선택
        binding.btnElderType.setOnClickListener {
            if (isGuardianClicked) isGuardianClicked = !isGuardianClicked
            isElderClicked = !isElderClicked

            if (isElderClicked) {
                binding.btnElderType.setBackgroundResource(R.drawable.rectangle_green_radius_20)
                binding.btnElderType.setTextColor(resources.getColor(R.color.white, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnElderType,
                    ColorStateList.valueOf(resources.getColor(R.color.white, null))
                )

                binding.btnNext.isEnabled = true

                // 보호자 버튼
                binding.btnGuardianType.setBackgroundResource(R.drawable.rectangle_white_border_gray600_radius_20)
                binding.btnGuardianType.setTextColor(resources.getColor(R.color.black, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnGuardianType,
                    ColorStateList.valueOf(resources.getColor(R.color.black, null))
                )
            } else {
                binding.btnElderType.setBackgroundResource(R.drawable.rectangle_white_border_gray600_radius_20)
                binding.btnElderType.setTextColor(resources.getColor(R.color.black, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnElderType,
                    ColorStateList.valueOf(resources.getColor(R.color.black, null))
                )

                binding.btnNext.isEnabled = false
            }
        }

        // 보호자 버튼 선택
        binding.btnGuardianType.setOnClickListener {
            if (isElderClicked) isElderClicked = !isElderClicked
            isGuardianClicked = !isGuardianClicked

            // 추후 dataBinding 으로 변경해서 구현하기
            if (isGuardianClicked) {
                binding.btnGuardianType.setBackgroundResource(R.drawable.rectangle_green_radius_20)
                binding.btnGuardianType.setTextColor(resources.getColor(R.color.white, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnGuardianType,
                    ColorStateList.valueOf(resources.getColor(R.color.white, null))
                )

                binding.btnNext.isEnabled = true

                // 어르신 버튼
                binding.btnElderType.setBackgroundResource(R.drawable.rectangle_white_border_gray600_radius_20)
                binding.btnElderType.setTextColor(resources.getColor(R.color.black, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnElderType,
                    ColorStateList.valueOf(resources.getColor(R.color.black, null))
                )
            } else {
                binding.btnGuardianType.setBackgroundResource(R.drawable.rectangle_white_border_gray600_radius_20)
                binding.btnGuardianType.setTextColor(resources.getColor(R.color.black, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnGuardianType,
                    ColorStateList.valueOf(resources.getColor(R.color.black, null))
                )

                binding.btnNext.isEnabled = false
            }
        }

        // 다음 버튼 선택
        binding.btnNext.setOnClickListener {
            val userType: String = if (isElderClicked) "elder" else "guardian"

            findNavController().navigate(
                UserTypeFragmentDirections.actionFragmentUserTypeToFragmentUserName(userType)
            )
        }


        return binding.root
    }

}