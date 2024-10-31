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

class TimeInAdapter(
    private val context: Context,
    private var timeInList: MutableList<TimeInDBStructure>,
    private val databaseReference: DatabaseReference
) : RecyclerView.Adapter<TimeInAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val time_: TextView = itemView.findViewById(R.id.time)
        val delete_: ImageView = itemView.findViewById(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.time_in_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val time_in = timeInList[position]
        holder.time_.text = time_in.time_in_stamp

        holder.delete_.setOnClickListener {
            val sessionId = time_in.session_id

            // Delete the item from Firebase
            databaseReference.child(sessionId).removeValue().addOnSuccessListener {
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()

                // Check if position is still valid and remove from the list
                if (position < timeInList.size) {
                    timeInList.removeAt(position)
                    notifyDataSetChanged()  // Refresh entire list to prevent index issues
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return timeInList.size
    }
}
