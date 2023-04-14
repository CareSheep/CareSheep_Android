package com.swu.caresheep.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.swu.caresheep.R
import com.swu.caresheep.databinding.FragmentUserGenderBinding

class UserGenderFragment : Fragment() {

    private lateinit var binding: FragmentUserGenderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserGenderBinding.inflate(inflater, container, false)

        binding.btnComplete.isEnabled = true
        binding.btnComplete.setOnClickListener {
            // 전달 값에 따라 환자 또는 보호자 홈 화면으로 이동
        }

        return binding.root
    }

}