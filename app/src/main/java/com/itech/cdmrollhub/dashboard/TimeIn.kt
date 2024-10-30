package com.itech.cdmrollhub.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.itech.cdmrollhub.databinding.FragmentTimeInBinding

class TimeIn : Fragment() {

    private lateinit var binding: FragmentTimeInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimeInBinding.inflate(inflater, container, false)
        return binding.root
    }
}