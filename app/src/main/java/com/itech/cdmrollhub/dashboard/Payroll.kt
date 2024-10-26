package com.itech.cdmrollhub.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.itech.cdmrollhub.databinding.FragmentPayrollBinding

class Payroll : Fragment() {

    private lateinit var binding: FragmentPayrollBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPayrollBinding.inflate(inflater, container, false)
        return binding.root
    }
}