package com.itech.cdmrollhub.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itech.cdmrollhub.AttendanceDBStructure
import com.itech.cdmrollhub.R
import com.itech.cdmrollhub.TimeInAdapter
import com.itech.cdmrollhub.TimeInDBStructure
import com.itech.cdmrollhub.databinding.FragmentTimeInBinding
import com.squareup.picasso.Picasso

class TimeIn : Fragment() {

    private lateinit var binding: FragmentTimeInBinding
    private lateinit var timeInAdapter: TimeInAdapter
    private lateinit var databaseReference: DatabaseReference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var timeInList: MutableList<TimeInDBStructure>
    private lateinit var noPostText: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimeInBinding.inflate(inflater, container, false)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return binding.root
        databaseReference = FirebaseDatabase.getInstance().getReference("EmployeeSessionTbl")
            .child(currentUserId)
            .child("TimeInLogs")

        timeInList = mutableListOf()
        timeInAdapter = TimeInAdapter(requireContext(), timeInList, databaseReference)
        binding.timeInList.adapter = timeInAdapter
        binding.timeInList.layoutManager = LinearLayoutManager(requireContext())

        noPostText = binding.noPostText

        loadTimeInData()
        displayProfilePicture()
        setupFragmentNavigation()

        return binding.root
    }

    private fun setupFragmentNavigation() {
        binding.notificationBttn.setOnClickListener {
            val notificationFragment = Notification()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, notificationFragment)
                .addToBackStack(null)
                .commit()
        }
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

    private fun loadTimeInData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                timeInList.clear()  // Clear previous data

                if (snapshot.exists()) {
                    for (sessionSnapshot in snapshot.children) {
                        val sessionId = sessionSnapshot.key ?: ""
                        val timInStamp = sessionSnapshot.getValue(String::class.java) ?: ""

                        if (sessionId.isNotEmpty() && timInStamp.isNotEmpty()) {
                            timeInList.add(TimeInDBStructure(session_id = sessionId, time_in_stamp = timInStamp))
                        }
                    }
                    timeInAdapter.notifyDataSetChanged()
                } else {
                    noPostText.visibility = View.VISIBLE
                }

                noPostText.visibility = if (timeInList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load time in records", Toast.LENGTH_SHORT).show()
            }
        })
    }
}