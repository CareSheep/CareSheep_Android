package com.swu.caresheep.ui.start

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.swu.caresheep.R
import com.swu.caresheep.databinding.FragmentUserGenderBinding


class UserGenderFragment : Fragment() {

    private lateinit var binding: FragmentUserGenderBinding
    private var isMaleClicked: Boolean = false
    private var isFemaleClicked: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserGenderBinding.inflate(inflater, container, false)


        // 남성 버튼 선택
        binding.btnMale.setOnClickListener {
            if (isFemaleClicked) isFemaleClicked = !isFemaleClicked
            isMaleClicked = !isMaleClicked

            // 추후 dataBinding 으로 변경해서 구현하기
            if (isMaleClicked) {
                binding.btnMale.setBackgroundResource(R.drawable.rectangle_green_radius_20)
                binding.btnMale.setTextColor(resources.getColor(R.color.white, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnMale,
                    ColorStateList.valueOf(resources.getColor(R.color.white, null))
                )

                binding.btnNext.isEnabled = true

                // 여성 버튼
                binding.btnFemale.setBackgroundResource(R.drawable.rectangle_white_border_gray600_radius_20)
                binding.btnFemale.setTextColor(resources.getColor(R.color.black, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnFemale,
                    ColorStateList.valueOf(resources.getColor(R.color.black, null))
                )
            } else {
                binding.btnMale.setBackgroundResource(R.drawable.rectangle_white_border_gray600_radius_20)
                binding.btnMale.setTextColor(resources.getColor(R.color.black, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnMale,
                    ColorStateList.valueOf(resources.getColor(R.color.black, null))
                )

                binding.btnNext.isEnabled = false
            }

        }

        // 여성 버튼 선택
        binding.btnFemale.setOnClickListener {
            if (isMaleClicked) isMaleClicked = !isMaleClicked
            isFemaleClicked = !isFemaleClicked

            // 추후 dataBinding 으로 변경해서 구현하기
            if (isFemaleClicked) {
                binding.btnFemale.setBackgroundResource(R.drawable.rectangle_green_radius_20)
                binding.btnFemale.setTextColor(resources.getColor(R.color.white, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnFemale,
                    ColorStateList.valueOf(resources.getColor(R.color.white, null))
                )

                binding.btnNext.isEnabled = true

                // 남성 버튼
                binding.btnMale.setBackgroundResource(R.drawable.rectangle_white_border_gray600_radius_20)
                binding.btnMale.setTextColor(resources.getColor(R.color.black, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnMale,
                    ColorStateList.valueOf(resources.getColor(R.color.black, null))
                )
            } else {
                binding.btnFemale.setBackgroundResource(R.drawable.rectangle_white_border_gray600_radius_20)
                binding.btnFemale.setTextColor(resources.getColor(R.color.black, null))
                TextViewCompat.setCompoundDrawableTintList(
                    binding.btnFemale,
                    ColorStateList.valueOf(resources.getColor(R.color.black, null))
                )

                binding.btnNext.isEnabled = false
            }

        }

        // 다음 버튼 선택
        binding.btnNext.setOnClickListener {
            val args = requireArguments()
            val userType = args.getString("user_type")
            val userName = args.getString("user_name")
            val userGender: String = if (isMaleClicked) "male" else "female"

            findNavController().navigate(
                UserGenderFragmentDirections.actionFragmentUserGenderToFragmentUserAge(
                    userType!!,
                    userName!!,
                    userGender
                )
            )
        }

        return binding.root
    }

}