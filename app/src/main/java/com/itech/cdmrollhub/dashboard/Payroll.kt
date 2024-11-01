package com.itech.cdmrollhub.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.itech.cdmrollhub.R
import com.itech.cdmrollhub.databinding.FragmentPayrollBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class Payroll : Fragment() {

    private lateinit var binding: FragmentPayrollBinding
    private lateinit var calendarView: CalendarView
    private lateinit var databaseRef: DatabaseReference
    private val databaseRef2 = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Define color drawables for event day indicators
    private val colorDrawables = listOf(
        R.drawable.green_event,
        R.drawable.blue_event,
        R.drawable.red_event
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayrollBinding.inflate(inflater, container, false)
        calendarView = binding.payPeriod
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database reference
        databaseRef = FirebaseDatabase.getInstance().getReference("EmployeePayrollDataTbl")

        fetchAndHighlightPayPeriods()
        fetchAndDisplayPayrollData()
        displayProfilePicture()
        setupFragmentNavigation()
        checkForNotifications()

        return binding.root
    }

    private fun checkForNotifications() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val notificationsRef = databaseRef2.child("NotificationTbl").child(currentUserId)

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

    private fun fetchAndDisplayPayrollData() {
        val userId = auth.currentUser?.uid ?: return

        // Reference to the user's payroll data
        val userRef = databaseRef.child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Fetch and display total hours
                    val totalHoursValue = snapshot.child("TotalHours/total_hours").getValue(Any::class.java)
                    binding.totalHours.text = totalHoursValue.toString() ?: "0"

                    // Fetch and display total overtime hours
                    val totalOTHoursValue = snapshot.child("TotalOTHours/total_ot_hours").getValue(Any::class.java)
                    binding.totalOTHours.text = totalOTHoursValue.toString() ?: "0"

                    // Fetch and display total salary
                    val totalSalaryValue = snapshot.child("TotalSalary/total_salary").getValue(Any::class.java)
                    binding.totalSalary.text = totalSalaryValue.toString() ?: "0"

                    // Fetch and display total years
                    val totalYearValue = snapshot.child("TotalYear/total_year").getValue(Any::class.java)
                    binding.totalYear.text = totalYearValue.toString() ?: "0"
                } else {
                    Toast.makeText(context, "No payroll data found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAndHighlightPayPeriods() {
        val userId = auth.currentUser?.uid ?: return

        // Reference to the user's PayPeriod data
        val payPeriodRef = databaseRef.child(userId).child("PayPeriod")

        payPeriodRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val payDates = mutableListOf<Date>()
                    for (dateSnapshot in snapshot.children) {
                        val dateString = dateSnapshot.getValue(String::class.java)
                        dateString?.let {
                            val date = dateFormat.parse(it)
                            date?.let { parsedDate ->
                                payDates.add(parsedDate)
                            }
                        }
                    }
                    highlightPayPeriods(payDates)
                } else {
                    Toast.makeText(context, "No pay periods found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun highlightPayPeriods(payDates: List<Date>) {
        val events = mutableListOf<EventDay>()
        var colorIndex = 0
        var count = 0

        payDates.sorted().forEach { date ->
            // For each 15-day cycle, change the highlight color
            if (count == 15) {
                colorIndex = (colorIndex + 1) % colorDrawables.size
                count = 0
            }

            // Create Calendar object for each pay date and set EventDay with color drawable
            val calendar = Calendar.getInstance().apply { time = date }
            val eventDay = EventDay(calendar, colorDrawables[colorIndex])
            events.add(eventDay)

            count++
        }

        // Set events in CalendarView
        calendarView.setEvents(events)
    }
}
