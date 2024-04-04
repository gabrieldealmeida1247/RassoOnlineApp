package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageService
import com.example.rassoonlineapp.R

class ManageServiceWorkerAdapter(private val context: Context, private val manageServices: List<ManageService>) : RecyclerView.Adapter<ManageServiceWorkerAdapter.ManageServiceViewHolder>() {

    inner class ManageServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceId: TextView = itemView.findViewById(R.id.service_id_text)
        val money: TextView = itemView.findViewById(R.id.money_text)
        val projectDate: TextView = itemView.findViewById(R.id.project_date_text)
        val status: TextView = itemView.findViewById(R.id.text_view_status)
        val projectName: TextView = itemView.findViewById(R.id.textView_project_name)
        val clientName: TextView = itemView.findViewById(R.id.textView_client_name)
        val  expirationDate: TextView = itemView.findViewById(R.id.textView_expiration_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageServiceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manage_service_worker_item_layout, parent, false)
        return ManageServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageServiceViewHolder, position: Int) {
        val currentManageService = manageServices[position]
        holder.serviceId.text = "Service ID: ${currentManageService.serviceId}"
        holder.money.text = "Money: ${currentManageService.money}"
        holder.projectDate.text = "Project Date: ${currentManageService.projectDate}"
        holder.status.text =  "Estado: ${currentManageService.status}"
        holder.projectName.text = "${currentManageService.projectName}"
        holder.clientName.text = "Cliente: ${currentManageService.clientName}"
        holder.expirationDate.text = "Prazo: ${currentManageService.expirationDate}"

        // Adicione logs para verificar se os valores estão nulos
        Log.d("ManageServiceAdapter", "Service ID: ${currentManageService.serviceId}")
        Log.d("ManageServiceAdapter", "Money: ${currentManageService.money}")
        Log.d("ManageServiceAdapter", "Project Date: ${currentManageService.projectDate}")
        Log.d("ManageServiceAdapter", "Estado: ${currentManageService.status}")
        Log.d("ManageServiceAdapter", "Project Name: ${currentManageService.projectName}")
        Log.d("ManageServiceAdapter", "Client Name: ${currentManageService.clientName}")
        Log.d("ManageServiceAdapter", "Expiration Date: ${currentManageService.expirationDate}")
    }

    override fun getItemCount(): Int {
        return manageServices.size
    }
}
