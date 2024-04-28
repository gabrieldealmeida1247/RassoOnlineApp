package com.example.rassoonlineapp.Admin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Admin.model.AdminNotification
import com.example.rassoonlineapp.R

class AdminNotificationAdapter(private val mContext: Context,
    private val notifications: List<AdminNotification>
) : RecyclerView.Adapter<AdminNotificationAdapter.AdminNotificationViewHolder>() {

    inner class AdminNotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_admin_notification)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.description_admin_notification)

        fun bind(notification: AdminNotification) {
            usernameTextView.text = "Admin"
            descriptionTextView.text = notification.textoFeed
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminNotificationViewHolder {
        val itemView = LayoutInflater.from(mContext)
            .inflate(R.layout.admin_notification_item_layout, parent, false)
        return AdminNotificationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AdminNotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size
}
