package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Statistic
import com.example.rassoonlineapp.R

class ServiceStatisticAdapter(
    private val context: Context, private val statistic: List<Statistic>
) :
    RecyclerView.Adapter<ServiceStatisticAdapter.ServiceStatisticViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceStatisticViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.service_item_layout, parent, false)
        return ServiceStatisticViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceStatisticViewHolder, position: Int) {
        val currentUser = statistic[position]

        holder.textViewPostCount.text = "Serviços publicados: ${currentUser.postsCount}"

    }

    override fun getItemCount() = statistic.size

    inner class ServiceStatisticViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewPostCount: TextView = itemView.findViewById(R.id.textView_qtd_sender_service)
        val textViewProjectTitle: TextView = itemView.findViewById(R.id.textView_profile_tittle)
        val textViewStatus: TextView = itemView.findViewById(R.id.textView_status)
    }
}
