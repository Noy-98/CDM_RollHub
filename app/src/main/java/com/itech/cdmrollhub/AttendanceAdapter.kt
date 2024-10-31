package com.itech.cdmrollhub

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttendanceAdapter(private val context: Context, private var attendanceList: List<AttendanceDBStructure>) :
    RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

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

        }
    }

    override fun getItemCount(): Int {
        return attendanceList.size
    }
}