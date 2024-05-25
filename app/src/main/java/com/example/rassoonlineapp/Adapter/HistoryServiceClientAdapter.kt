package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.Model.ManageServiceHistory
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryServiceClientAdapter(private val context: Context, private val manageServiceHistory: List<ManageServiceHistory>) : RecyclerView.Adapter<HistoryServiceClientAdapter.ManageServiceHistoryViewHolder>() {

    inner class ManageServiceHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val money: TextView = itemView.findViewById(R.id.money_text)
        val projectDate: TextView = itemView.findViewById(R.id.project_date_text)
        val status: TextView = itemView.findViewById(R.id.text_view_status)
        val projectName: TextView = itemView.findViewById(R.id.textView_project_name)
        val workerName: TextView = itemView.findViewById(R.id.textView_worker_name)
        val expirationDate: TextView = itemView.findViewById(R.id.textView_expiration_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageServiceHistoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.history_service_client_item_layout, parent, false)

        return ManageServiceHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageServiceHistoryViewHolder, position: Int) {
        val currentManageService = manageServiceHistory[position]

        holder.money.text = "dinheiro: ${currentManageService.money}"
        holder.projectDate.text = "Data: ${currentManageService.createdAt}"
       // holder.status.text =  "Estado: ${currentManageService.status}"
        holder.projectName.text = "${currentManageService.projectName}"
        holder.workerName.text = "Trabalhador: ${currentManageService.workerName}"
        holder.expirationDate.text = "Prazo: ${currentManageService.expirationDate}"


        // Obtendo o status do ManageProject da base de dados
        val manageProjectRef = FirebaseDatabase.getInstance().reference.child("ManageProject").child(currentManageService.serviceHistoryId)
        manageProjectRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val manageProject = snapshot.getValue(ManageProject::class.java)
                val status = manageProject?.status ?: "Ativo"
                holder.status.text = "Estado: $status"
            }

            override fun onCancelled(error: DatabaseError) {
                // Tratar erro de leitura do banco de dados, se necessário
            }
        })

    }

    override fun getItemCount(): Int {
        return manageServiceHistory.size
    }
}
