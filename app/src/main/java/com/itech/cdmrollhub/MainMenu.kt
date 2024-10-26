package com.itech.cdmrollhub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.itech.cdmrollhub.dashboard.Home
import com.itech.cdmrollhub.dashboard.Logout
import com.itech.cdmrollhub.dashboard.Payroll
import com.itech.cdmrollhub.dashboard.Profile
import com.itech.cdmrollhub.databinding.ActivityMainMenuBinding

class MainMenu : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        loadFragment(Home())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.dashboard

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> {
                    loadFragment(Home())
                    true
                }
                R.id.payroll -> {
                    loadFragment(Payroll())
                    true
                }
                R.id.profile -> {
                    loadFragment(Profile())
                    true
                }
                R.id.logout -> {
                    loadFragment(Logout())
                    true
                }
                else -> false
            }
        }
    }
}