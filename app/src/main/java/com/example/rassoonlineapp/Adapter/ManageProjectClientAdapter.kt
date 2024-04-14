package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.R

class ManageProjectClientAdapter(private val context: Context, private val manageProject: List<ManageProject>) :
    RecyclerView.Adapter<ManageProjectClientAdapter.ManageProjectViewHolder>() {

    inner class ManageProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views here if needed
        val projectName = itemView.findViewById<TextView>(R.id.textView_project_name)
        val projectDescription = itemView.findViewById<TextView>(R.id.textView_project_description_client)
        val projectClientSkills = itemView.findViewById<TextView>(R.id.textView_skills_projects_client)
        val clientName = itemView.findViewById<TextView>(R.id.textView_project_name_client)
        val workerName = itemView.findViewById<TextView>(R.id.textView_project_name_worker)
        val projectInicialDate = itemView.findViewById<TextView>(R.id.textView_inicial_client_date)
        val projectPay = itemView.findViewById<TextView>(R.id.textView_client_pay)
        val projectClientTermino = itemView.findViewById<TextView>(R.id.client_termino)
        val projectClientRestante = itemView.findViewById<TextView>(R.id.client_tempoRestante)
        val clientStatus = itemView.findViewById<TextView>(R.id.textView_client_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageProjectViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manage_project_client_item_layout, parent, false)
        return ManageProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageProjectViewHolder, position: Int) {
        val currentManageProject = manageProject[position]
        // Bind data to views here if needed
        holder.projectName.text = "${currentManageProject.projectName}"
        holder.projectDescription.text = "${currentManageProject.description}"
        holder.projectClientSkills.text = "${currentManageProject.skills}"
        holder.clientName.text = "${currentManageProject.clientName}"
        holder.workerName.text = "${currentManageProject.workerName}"
        holder.projectInicialDate.text = "PRAZO: ${currentManageProject.prazo}"
        holder.clientStatus.text = "${currentManageProject.status}"
        holder.projectPay.text = "Pagamento: ${currentManageProject.pay}"
        holder.projectClientTermino.text = "${currentManageProject.prazoTermino}"
        holder.projectClientRestante.text = "${currentManageProject.tempoRestante}"
    }

    override fun getItemCount(): Int {
        return manageProject.size
    }
}