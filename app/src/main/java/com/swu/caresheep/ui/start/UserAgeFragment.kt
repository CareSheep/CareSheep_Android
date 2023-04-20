package com.swu.caresheep.ui.start

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.swu.caresheep.databinding.FragmentUserAgeBinding

class UserAgeFragment : Fragment() {

    private lateinit var binding: FragmentUserAgeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserAgeBinding.inflate(inflater, container, false)


        return binding.root
    }

}