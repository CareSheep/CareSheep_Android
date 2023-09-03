package com.swu.caresheep.start

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.swu.caresheep.databinding.FragmentUserNameBinding
import com.swu.caresheep.start.UserNameFragmentDirections

class UserNameFragment : Fragment() {

    private lateinit var binding: FragmentUserNameBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserNameBinding.inflate(inflater, container, false)

        binding.etUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.btnNext.isEnabled = false
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.btnNext.isEnabled = p0?.isNotEmpty() == true
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })


        binding.btnNext.setOnClickListener {
            val args = requireArguments()
            val userType = args.getString("user_type")

            val userName = binding.etUserName.text.toString()
            findNavController().navigate(
                UserNameFragmentDirections.actionFragmentUserNameToFragmentUserGender(
                    userType!!,
                    userName
                )
            )
        }

        return binding.root
    }

}