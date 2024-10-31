package com.itech.cdmrollhub.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itech.cdmrollhub.R
import com.itech.cdmrollhub.databinding.FragmentStatusBinding
import com.squareup.picasso.Picasso

class Status : Fragment() {

    private lateinit var binding: FragmentStatusBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseRef = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatusBinding.inflate(inflater, container, false)


        fetchAndDisplayEmployeeData()
        displayProfilePicture()
        setupFragmentNavigation()
        checkForNotifications()

        return binding.root
    }

    private fun checkForNotifications() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val notificationsRef = databaseRef.child("NotificationTbl").child(currentUserId)

        notificationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if the snapshot has any children (notifications)
                if (snapshot.exists() && snapshot.hasChildren()) {
                    // Change ImageView color to red if there are notifications
                    binding.notificationBttn.setColorFilter(resources.getColor(R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
                } else {
                    // Reset ImageView color if no notifications
                    binding.notificationBttn.clearColorFilter()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("checkForNotifications", "Error fetching notifications: ${error.message}")
            }
        })
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

    private fun fetchAndDisplayEmployeeData() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return

        // Reference to the current user's employment status in EmployeeDataTbl
        val employeeStatusRef = databaseRef.child("EmployeeDataTbl").child(currentUserId)

        employeeStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    val first_name = snapshot.child("first_name").getValue(String::class.java) ?: ""
                    val last_name = snapshot.child("last_name").getValue(String::class.java) ?: ""
                    val employee_id = snapshot.child("employee_id").getValue(String::class.java) ?: ""
                    val mobile_number = snapshot.child("mobile_number").getValue(String::class.java) ?: ""
                    val home_address = snapshot.child("home_address").getValue(String::class.java) ?: ""
                    val civil_status = snapshot.child("civil_status").getValue(String::class.java) ?: ""
                    val age = snapshot.child("age").getValue(String::class.java) ?: ""
                    val date_of_birth = snapshot.child("date_of_birth").getValue(String::class.java) ?: ""
                    val date_hired = snapshot.child("date_hired").getValue(String::class.java) ?: ""
                    val department = snapshot.child("department").getValue(String::class.java) ?: ""
                    val employment_status = snapshot.child("employment_status").getValue(String::class.java) ?: ""
                    val email = snapshot.child("email_address").getValue(String::class.java) ?: ""
                    val employee_profile_picture = snapshot.child("profile_picture").getValue(String::class.java)

                    if (!employee_profile_picture.isNullOrEmpty()) {
                        Picasso.get().load(employee_profile_picture).into(binding.employeeProfilePic)
                    } else {
                        binding.employeeProfilePic.setImageResource(R.drawable.camera_icon)
                    }

                    binding.firstName.text = first_name
                    binding.lastName.text = last_name
                    binding.employeeID.text = employee_id
                    binding.mobileNumber.text = mobile_number
                    binding.homeAddress.text = home_address
                    binding.civilStatus.text = civil_status
                    binding.age.text = age
                    binding.dateOfBirth.text = date_of_birth
                    binding.dateHired.text = date_hired
                    binding.department.text = department
                    binding.employmentStatus.text = employment_status
                    binding.email.text = email

                } else {

                    Toast.makeText(requireContext(), "Employee data not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchAndDisplayEmployeeStatus", "Error fetching employee data: ${error.message}")
                Toast.makeText(requireContext(), "Error fetching employee data.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}