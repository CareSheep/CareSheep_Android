package com.swu.caresheep.ui.guardian.calendar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.swu.caresheep.databinding.FragmentGuardianCalendarBinding

class GuardianCalendarFragment : Fragment() {

    private lateinit var binding: FragmentGuardianCalendarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuardianCalendarBinding.inflate(inflater, container, false)



        return binding.root
    }

}