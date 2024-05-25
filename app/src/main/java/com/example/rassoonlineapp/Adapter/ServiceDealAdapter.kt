package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Contract
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.ManageContractClientActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ServiceDealAdapter(private val context: Context, private val serviceDeal: List<Contract>) : RecyclerView.Adapter<ServiceDealAdapter.ServiceDealViewHolder>() {

    inner class ServiceDealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.textView_project_name_deal)
        val workerName: TextView = itemView.findViewById(R.id.textView_worker_name_deal)
        val projectDate: TextView = itemView.findViewById(R.id.project_date_text_deal)
        val status: TextView = itemView.findViewById(R.id.text_view_status_deal)
        val expirationDate: TextView = itemView.findViewById(R.id.textView_expiration_date_deal)
        val money: TextView = itemView.findViewById(R.id.money_text_deal)
        val cardView: CardView = itemView.findViewById(R.id.cardView_client_contract)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceDealViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.service_deal_item_layout, parent, false)
        return ServiceDealViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceDealViewHolder, position: Int) {
        val currentServiceDeal = serviceDeal[position]

        holder.projectName.text = currentServiceDeal.projectName
        holder.workerName.text = "Trabalhador: ${currentServiceDeal.workerName}"
        holder.projectDate.text = "Data do projeto: ${currentServiceDeal.projectDate}"
        holder.status.text = "Estado: ${currentServiceDeal.status}"
        holder.expirationDate.text = "Prazo: ${currentServiceDeal.expirationDate}"
        holder.money.text = "Dinheiro: ${currentServiceDeal.money}"

        holder.cardView.setOnClickListener {
            // Consulta ao Firebase para encontrar o manageContractId correspondente ao contractId clicado
            val databaseReference = FirebaseDatabase.getInstance().reference.child("ManageContracts")
            val query = databaseReference.orderByChild("contractId").equalTo(currentServiceDeal.contractId)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Existem contratos correspondentes ao contractId clicado
                        // Supondo que apenas um contrato correspondente será retornado
                        val manageContractId = dataSnapshot.children.first().key

                        // Inicia a ManageContractClientActivity com o manageContractId
                        val intent = Intent(context, ManageContractClientActivity::class.java).apply {
                            putExtra("manageContractId", manageContractId)
                        }
                        context.startActivity(intent)
                    } else {
                        // Não há contrato correspondente ao contractId clicado
                        Log.d("ServiceDealAdapter", "Nenhum contrato correspondente encontrado para contractId: ${currentServiceDeal.contractId}")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("ServiceDealAdapter", "Failed to retrieve manageContractId: ${databaseError.message}")
                }
            })
        }

    }

    override fun getItemCount(): Int {
        return serviceDeal.size
    }
}
