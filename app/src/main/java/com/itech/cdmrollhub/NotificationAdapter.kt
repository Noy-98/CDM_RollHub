package com.itech.cdmrollhub

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class NotificationAdapter(
    private val context: Context,
    private var notificationList: MutableList<NotificationDBStructure>,
    private val databaseReference: DatabaseReference,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title_: TextView = itemView.findViewById(R.id.title)
        val message_: TextView = itemView.findViewById(R.id.message)
        val delete_: ImageView = itemView.findViewById(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Ensure the position is valid before accessing the notificationList
        if (position >= notificationList.size) return

        val notification = notificationList[position]

        holder.title_.text = notification.title
        holder.message_.text = notification.message

        holder.delete_.setOnClickListener {
            // Store the current position
            val currentPosition = position

            // Use the unique key of the notification to delete it
            val notificationId = notification.id // This is the unique identifier for deletion
            databaseReference.child(notificationId).removeValue()
                .addOnSuccessListener {
                    // Remove the notification from the list and notify the adapter
                    if (currentPosition < notificationList.size) { // Check the position again before accessing the list
                        notificationList.removeAt(currentPosition)
                        notifyItemRemoved(currentPosition) // Notify that an item was removed
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}
