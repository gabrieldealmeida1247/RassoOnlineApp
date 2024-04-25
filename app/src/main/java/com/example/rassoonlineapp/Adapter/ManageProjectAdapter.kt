package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.UsersActivity

class ManageProjectAdapter(private val context: Context, private val manageProject: List<ManageProject>) :
    RecyclerView.Adapter<ManageProjectAdapter.ManageProjectViewHolder>() {

    inner class ManageProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views here if needed
        val projectName = itemView.findViewById<TextView>(R.id.textView_project_name)
        val projectDescription = itemView.findViewById<TextView>(R.id.textView_project_description)
        val projectSkills = itemView.findViewById<TextView>(R.id.textView_skills_projects)
        val clientName = itemView.findViewById<TextView>(R.id.textView_project_name_client)
        val workerName = itemView.findViewById<TextView>(R.id.textView_project_name_worker)
        val projectInicialDate = itemView.findViewById<TextView>(R.id.textView_inicial_date)
        val projectPay = itemView.findViewById<TextView>(R.id.textView_pay)
        val projectTermino = itemView.findViewById<TextView>(R.id.termino)
        val projectRestante = itemView.findViewById<TextView>(R.id.tempoRestante)
        val estado = itemView.findViewById<TextView>(R.id.textView_estado)
        val chatButton = itemView.findViewById<TextView>(R.id.button_chat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageProjectViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manage_project_item_layout, parent, false)
        return ManageProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageProjectViewHolder, position: Int) {
        val currentManageProject = manageProject[position]
        // Bind data to views here if needed
        holder.projectName.text = "${currentManageProject.projectName}"
        holder.projectDescription.text = "${currentManageProject.description}"
        holder.projectSkills.text = "${currentManageProject.skills}"
        holder.clientName.text = "${currentManageProject.clientName}"
        holder.workerName.text = "${currentManageProject.workerName}"
        holder.projectInicialDate.text = "Início: ${currentManageProject.prazo}"
        holder.estado.text = "${currentManageProject.status}"
        holder.projectPay.text = "Pagamento: ${currentManageProject.pay}"
        holder.projectTermino.text = "${currentManageProject.prazoTermino}"
        holder.projectRestante.text = "${currentManageProject.tempoRestante}"



        holder.chatButton.setOnClickListener {
            val intent = Intent(context, UsersActivity::class.java)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return manageProject.size
    }
}