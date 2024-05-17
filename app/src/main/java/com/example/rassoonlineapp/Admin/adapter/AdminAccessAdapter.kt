package com.example.rassoonlineapp.Admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.R

class AdminAccessAdapter :RecyclerView.Adapter<AdminAccessAdapter.AdminAccessViewHolder>() {

    inner class AdminAccessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminAccessViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_access_item_layout, parent, false)
        return AdminAccessViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AdminAccessViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return 1
    }


}