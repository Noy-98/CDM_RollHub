package com.itech.cdmrollhub

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference

class AttendanceAdapter(
    private val context: Context,
    private var attendanceList: MutableList<AttendanceDBStructure>,
    private val databaseReference: DatabaseReference
) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date_: TextView = itemView.findViewById(R.id.date)
        val delete_: ImageView = itemView.findViewById(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.attendance_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attendance = attendanceList[position]
        holder.date_.text = attendance.date_stamp

        holder.delete_.setOnClickListener {
            val sessionId = attendance.session_id

            // Delete the item from Firebase
            databaseReference.child(sessionId).removeValue().addOnSuccessListener {
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()

                // Check if position is still valid and remove from the list
                if (position < attendanceList.size) {
                    attendanceList.removeAt(position)
                    notifyDataSetChanged()  // Refresh entire list to prevent index issues
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return attendanceList.size
    }
}
