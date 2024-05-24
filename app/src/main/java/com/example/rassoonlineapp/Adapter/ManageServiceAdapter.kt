package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.Model.ManageService
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.ManageProjectClientActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageServiceAdapter(private val context: Context, private val manageServices: List<ManageService>) : RecyclerView.Adapter<ManageServiceAdapter.ManageServiceViewHolder>() {

    inner class ManageServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val money: TextView = itemView.findViewById(R.id.money_text)
        val projectDate: TextView = itemView.findViewById(R.id.project_date_text)
        val status: TextView = itemView.findViewById(R.id.text_view_status)
        val projectName: TextView = itemView.findViewById(R.id.textView_project_name)
        val workerName: TextView = itemView.findViewById(R.id.textView_worker_name)
        val  expirationDate: TextView = itemView.findViewById(R.id.textView_expiration_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageServiceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manage_service_item_layout, parent, false)

        return ManageServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageServiceViewHolder, position: Int) {
        val currentManageService = manageServices[position]

        holder.money.text = "Money: ${currentManageService.money}"
        holder.projectDate.text = "Data do projecto: ${currentManageService.createdAt}"
        holder.status.text =  "Estado: ${currentManageService.status}"
        holder.projectName.text = "${currentManageService.projectName}"
        holder.workerName.text = "Trabalhador: ${currentManageService.workerName}"
        holder.expirationDate.text = "Data do projecto: ${currentManageService.expirationDate}"



        // Obtendo o status do ManageProject da base de dados
        val manageProjectRef = FirebaseDatabase.getInstance().reference.child("ManageProject").child(currentManageService.serviceId)
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


        holder.itemView.findViewById<CardView>(R.id.cardView_client_project).setOnClickListener {
            val intent = Intent(context, ManageProjectClientActivity::class.java)
            intent.putExtra("manageId", currentManageService.serviceId) // Passando o manageId como um extra
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return manageServices.size
    }
}
