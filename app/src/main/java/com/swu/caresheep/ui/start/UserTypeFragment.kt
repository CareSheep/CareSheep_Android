package com.swu.caresheep.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.swu.caresheep.databinding.FragmentUserTypeBinding

class UserTypeFragment : Fragment() {

    private lateinit var binding: FragmentUserTypeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserTypeBinding.inflate(inflater, container, false)

        // 어르신 버튼 선택
        binding.btnElderType.setOnClickListener {
            findNavController().navigate(
                UserTypeFragmentDirections.actionFragmentUserTypeToFragmentUserName()
            )
        }

        // 보호자 버튼 선택
        binding.btnGuardianType.setOnClickListener {
            findNavController().navigate(
                UserTypeFragmentDirections.actionFragmentUserTypeToFragmentUserName()
            )
        }

        return binding.root
    }

}