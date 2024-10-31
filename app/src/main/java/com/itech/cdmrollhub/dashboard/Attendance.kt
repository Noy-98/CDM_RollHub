package com.itech.cdmrollhub.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.itech.cdmrollhub.AttendanceAdapter
import com.itech.cdmrollhub.AttendanceDBStructure
import com.itech.cdmrollhub.databinding.FragmentAttendanceBinding

class Attendance : Fragment() {

    private lateinit var binding: FragmentAttendanceBinding
    private lateinit var attendanceAdapter: AttendanceAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var attendanceList: MutableList<AttendanceDBStructure>
    private lateinit var noPostText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAttendanceBinding.inflate(inflater, container, false)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return binding.root
        databaseReference = FirebaseDatabase.getInstance().getReference("EmployeeSessionTbl")
            .child(currentUserId)
            .child("DateLogs")

        attendanceList = mutableListOf()
        attendanceAdapter = AttendanceAdapter(requireContext(), attendanceList, databaseReference)
        binding.attendanceList.adapter = attendanceAdapter
        binding.attendanceList.layoutManager = LinearLayoutManager(requireContext())

        noPostText = binding.noPostText

        loadAttendanceData()

        return binding.root
    }

    private fun loadAttendanceData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                attendanceList.clear()  // Clear previous data

                if (snapshot.exists()) {
                    for (sessionSnapshot in snapshot.children) {
                        val sessionId = sessionSnapshot.key ?: ""
                        val dateStamp = sessionSnapshot.getValue(String::class.java) ?: ""

                        if (sessionId.isNotEmpty() && dateStamp.isNotEmpty()) {
                            attendanceList.add(AttendanceDBStructure(session_id = sessionId, date_stamp = dateStamp))
                        }
                    }
                    attendanceAdapter.notifyDataSetChanged()
                } else {
                    noPostText.visibility = View.VISIBLE
                }

                noPostText.visibility = if (attendanceList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load attendance records", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
