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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val timeHandler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        displayProfilePicture()
        startUpdatingTime()
        fetchEmployeeSession()
        fetchAndDisplayTotalCounts()
        fetchAndDisplayEmployeeStatus()
        checkForNotifications()

       /* binding.resetBttn.setOnClickListener { resetEmployeeSession() } */

        setupFragmentNavigation()

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

    private fun fetchAndDisplayEmployeeStatus() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return

        // Reference to the current user's employment status in EmployeeDataTbl
        val employeeStatusRef = databaseRef.child("EmployeeDataTbl").child(currentUserId).child("employment_status")

        employeeStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get the employment status and display it
                    val employmentStatus = snapshot.getValue(String::class.java) ?: "Unknown"
                    binding.employeeStatus.text = employmentStatus
                } else {
                    // Show "Unknown" if employment status does not exist
                    binding.employeeStatus.text = "Unknown"
                    Toast.makeText(requireContext(), "Employment status not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchAndDisplayEmployeeStatus", "Error fetching employment status: ${error.message}")
                Toast.makeText(requireContext(), "Error fetching employment status.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAndDisplayTotalCounts() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return

        // Paths for each count category
        val dateLogsRef = databaseRef.child("EmployeeSessionTbl").child(currentUserId).child("DateLogs")
        val timeInLogsRef = databaseRef.child("EmployeeSessionTbl").child(currentUserId).child("TimeInLogs")
        val timeOutLogsRef = databaseRef.child("EmployeeSessionTbl").child(currentUserId).child("TimeOutLogs")

        // Fetch and count DateLogs sessions
        dateLogsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalAttendanceCount = snapshot.childrenCount
                binding.totalAttendance.text = totalAttendanceCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchAndDisplayTotalCounts", "Error fetching DateLogs: ${error.message}")
            }
        })

        // Fetch and count TimeInLogs sessions
        timeInLogsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalTimeInCount = snapshot.childrenCount
                binding.totalTimeIn.text = totalTimeInCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchAndDisplayTotalCounts", "Error fetching TimeInLogs: ${error.message}")
            }
        })

        // Fetch and count TimeOutLogs sessions
        timeOutLogsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalTimeOutCount = snapshot.childrenCount
                binding.totalTimeOut.text = totalTimeOutCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchAndDisplayTotalCounts", "Error fetching TimeOutLogs: ${error.message}")
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

        binding.attendanceBttn.setOnClickListener {
            val attendanceFragment = Attendance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, attendanceFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.statusBttn.setOnClickListener {
            val statusFragment = Status()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, statusFragment)
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
    }

   /* private fun resetEmployeeSession() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val sessionRef = databaseRef.child("EmployeeSessionTbl").child(currentUserId).child("Session")

        sessionRef.setValue(
            mapOf(
                "date_stamp" to "YYYY-MM-DD",
                "session_id" to "0",
                "time_in_stamp" to "00:00",
                "time_out_stamp" to "00:00"
            )
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Session data reset.", Toast.LENGTH_SHORT).show()
                binding.checkInTime.text = "00:00"
                binding.checkOutTime.text = "00:00"
            } else {
                Toast.makeText(requireContext(), "Failed to reset session data.", Toast.LENGTH_SHORT).show()
            }
        }
    } */

    private fun fetchEmployeeSession() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val sessionRef = databaseRef.child("EmployeeSessionTbl").child(currentUserId).child("Session")

        sessionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val timeIn = snapshot.child("time_in_stamp").getValue(String::class.java) ?: "00:00"
                    val timeOut = snapshot.child("time_out_stamp").getValue(String::class.java) ?: "00:00"

                    binding.checkInTime.text = timeIn
                    binding.checkOutTime.text = timeOut
                } else {
                    Toast.makeText(requireContext(), "No session data found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching session data.", Toast.LENGTH_SHORT).show()
                Log.e("fetchEmployeeSession", "Error: ${error.message}")
            }
        })
    }

    private fun startUpdatingTime() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val amPmFormat = SimpleDateFormat("a", Locale.getDefault())

        timeRunnable = object : Runnable {
            override fun run() {
                val currentTime = Date()
                binding.time.text = timeFormat.format(currentTime)
                binding.timeStatus.text = amPmFormat.format(currentTime)
                timeHandler.postDelayed(this, 1000)
            }
        }
        timeHandler.post(timeRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timeHandler.removeCallbacks(timeRunnable) // Stop updating time when view is destroyed
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