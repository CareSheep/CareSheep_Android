package com.swu.caresheep.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.swu.caresheep.databinding.FragmentUserNameBinding

class UserNameFragment : Fragment() {

    private lateinit var binding: FragmentUserNameBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserNameBinding.inflate(inflater, container, false)

        binding.btnNext.isEnabled = true
        binding.btnNext.setOnClickListener {
            findNavController().navigate(
                UserNameFragmentDirections.actionFragmentUserNameToFragmentUserGender()
            )
        }

        return binding.root
    }

}