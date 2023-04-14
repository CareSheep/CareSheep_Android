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
            val navGraph = findNavController().graph
            navGraph.setStartDestination(R.id.activity_start)
            findNavController().setGraph(R.navigation.main_nav_graph)
        }

        return binding.root
    }

}