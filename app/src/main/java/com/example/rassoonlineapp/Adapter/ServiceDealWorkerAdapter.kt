package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Contract
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.ManageContractWorkerActivity

class ServiceDealWorkerAdapter(private val context: Context, private val serviceDeal: List<Contract>) : RecyclerView.Adapter<ServiceDealWorkerAdapter.ServiceDealWorkerViewHolder>() {

    inner class ServiceDealWorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.textView_project_name_deal)
        val clientName: TextView = itemView.findViewById(R.id.textView_client_name_deal)
        val projectDate: TextView = itemView.findViewById(R.id.project_date_text_deal)
        val status: TextView = itemView.findViewById(R.id.text_view_status_deal)
        val expirationDate: TextView = itemView.findViewById(R.id.textView_expiration_date_deal)
        val money: TextView = itemView.findViewById(R.id.money_text_deal)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceDealWorkerViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.service_deal_worker_item_layout, parent, false)

        return ServiceDealWorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceDealWorkerViewHolder, position: Int) {
        val currentServiceDeal = serviceDeal[position]

        holder.projectName.text = currentServiceDeal.projectName
        holder.clientName.text = "Cliente: ${currentServiceDeal.clientName}"
        holder.projectDate.text = "Data do Projecto${currentServiceDeal.projectDate}"
        holder.status.text = "Estado: ${currentServiceDeal.status}"
        holder.expirationDate.text = "Prazo: ${currentServiceDeal.expirationDate}"
        holder.money.text = "Dinheiro: ${currentServiceDeal.money}"

        // Definindo o clique no CardView
        holder.itemView.findViewById<CardView>(R.id.card_view_worker).setOnClickListener {
            val intent = Intent(context, ManageContractWorkerActivity::class.java)
            intent.putExtra(" manageContractId", currentServiceDeal.contractId)
         context.startActivity(intent)
        }
    }

        override fun getItemCount(): Int {
            return serviceDeal.size
        }
    }
