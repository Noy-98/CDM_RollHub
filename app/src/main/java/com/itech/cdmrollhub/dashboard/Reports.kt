package com.itech.cdmrollhub.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.itech.cdmrollhub.R
import com.itech.cdmrollhub.databinding.FragmentAttendanceBinding
import com.itech.cdmrollhub.databinding.FragmentReportsBinding

class Reports : Fragment() {

    private lateinit var binding: FragmentReportsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }
}