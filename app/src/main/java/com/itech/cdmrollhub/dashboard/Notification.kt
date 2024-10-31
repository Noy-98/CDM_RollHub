package com.itech.cdmrollhub.dashboard


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
import com.itech.cdmrollhub.NotificationAdapter
import com.itech.cdmrollhub.NotificationDBStructure
import com.itech.cdmrollhub.R
import com.itech.cdmrollhub.databinding.FragmentNotificationBinding
import com.squareup.picasso.Picasso

class Notification : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var databaseReference: DatabaseReference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var notificationList: MutableList<NotificationDBStructure>
    private lateinit var noPostText: TextView
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)

        currentUserId = firebaseAuth.currentUser?.uid ?: return binding.root
        databaseReference = FirebaseDatabase.getInstance().getReference("NotificationTbl").child(currentUserId)

        notificationList = mutableListOf()
        notificationAdapter = NotificationAdapter(requireContext(), notificationList, databaseReference)
        binding.notificationList.adapter = notificationAdapter
        binding.notificationList.layoutManager = LinearLayoutManager(requireContext())

        noPostText = binding.noPostText

        loadNotificationData()
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

    private fun loadNotificationData() {
        // Attach a ValueEventListener to the database reference
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationList.clear()  // Clear previous data

                if (snapshot.exists()) {
                    for (sessionSnapshot in snapshot.children) {
                        // Fetch title and message from each sessionSnapshot
                        val message = sessionSnapshot.child("message").getValue(String::class.java) ?: ""
                        val title = sessionSnapshot.child("title").getValue(String::class.java) ?: ""

                        // Ensure both title and message are not empty before adding
                        if (title.isNotEmpty() && message.isNotEmpty()) {
                            notificationList.add(
                                NotificationDBStructure(
                                    id = sessionSnapshot.key ?: "",  // Use the key of the notification as the id
                                    title = title,
                                    message = message
                                )
                            )
                        }
                    }
                    notificationAdapter.notifyDataSetChanged() // Notify adapter of changes
                } else {
                    noPostText.visibility = View.VISIBLE // Show no post text if there are no notifications
                }

                // Manage visibility of noPostText based on notification list size
                noPostText.visibility = if (notificationList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load notification records", Toast.LENGTH_SHORT).show()
            }
        })
    }
}