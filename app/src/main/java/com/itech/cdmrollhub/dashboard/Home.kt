package com.itech.cdmrollhub.dashboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ListenerRegistration
import com.itech.cdmrollhub.R
import com.itech.cdmrollhub.databinding.FragmentHomeBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val timeHandler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable
    private var userListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        displayProfilePicture()
        startUpdatingTime()

        binding.notificationBttn.setOnClickListener {
            val notificationFragment = Notification()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, notificationFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.attendanceBttn.setOnClickListener {
            val attendanceFragment = Attendance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, attendanceFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.historyBttn.setOnClickListener {
            val historyFragment = History()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, historyFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.timeInBttn.setOnClickListener {
            val timeInFragment = TimeIn()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, timeInFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.timeOutBttn.setOnClickListener {
            val timeOutFragment = TimeOut()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, timeOutFragment)
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    private fun startUpdatingTime() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeRunnable = object : Runnable {
            override fun run() {
                val currentTime = timeFormat.format(Date())
                binding.time.text = currentTime
                timeHandler.postDelayed(this, 1000)
            }
        }
        timeHandler.post(timeRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timeHandler.removeCallbacks(timeRunnable) // Stop updating time when view is destroyed
        userListener?.remove()
    }

    private fun displayProfilePicture() {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("usersTbl").child(currentUserId)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userMap = snapshot.value as? Map<String, Any?>
                    if (userMap != null) {
                        val profileImageUrl = snapshot.child("profilePicture").getValue(String::class.java)

                        if (!profileImageUrl.isNullOrEmpty()) {
                            Picasso.get().load(profileImageUrl).into(binding.profilePic)
                        } else {
                            binding.profilePic.setImageResource(R.drawable.camera_icon)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "User Profile Picture does not exist!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to retrieve Profile Picture", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("displayUser", "User is not logged in")
        }
    }
}