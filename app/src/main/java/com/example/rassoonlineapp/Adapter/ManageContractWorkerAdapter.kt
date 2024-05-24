package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageContract
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.UsersActivity

class ManageContractWorkerAdapter(private val context: Context, private val manageContractProject: List<ManageContract>) :
    RecyclerView.Adapter<ManageContractWorkerAdapter.ManageContractWorkerViewHolder>() {

    inner class ManageContractWorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameProject: TextView = itemView.findViewById(R.id.textView_project_name_contract)
        val projectDescription: TextView = itemView.findViewById(R.id.textView_project_description_contract)
        val clientName: TextView = itemView.findViewById(R.id.textView_project_name_client_contract)
        val workerName: TextView = itemView.findViewById(R.id.textView_project_name_worker_contract)
        val projectInicialDate: TextView = itemView.findViewById(R.id.textView_inicial_date_contract)
        val projectPay: TextView = itemView.findViewById(R.id.textView_pay_contract)
        val projectTermino: TextView = itemView.findViewById(R.id.termino_contract)
        val projectRestante: TextView = itemView.findViewById(R.id.tempoRestante_contract)
        val estado: TextView = itemView.findViewById(R.id.textView_estado_contract)
        val chatButton: ImageView = itemView.findViewById(R.id.button_chat_contract)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar_contract)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageContractWorkerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manage_contract_worker_item_layout, parent, false)
        return ManageContractWorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageContractWorkerViewHolder, position: Int) {
        val currentContract = manageContractProject[position]

        // Log for debugging
        Log.d("ManageContractWorkerAdapter", "projectName: ${currentContract.projectName}")
        Log.d("ManageContractWorkerAdapter", "projectDate: ${currentContract.projectDate}")

        // Bind data to views
        holder.nameProject.text = currentContract.projectName
        holder.projectDescription.text = currentContract.description
        holder.clientName.text = currentContract.clientName
        holder.workerName.text = currentContract.workerName
        holder.projectInicialDate.text = "Início: ${currentContract.projectDate}"
        holder.estado.text = currentContract.status
        holder.projectPay.text = "Pagamento: ${currentContract.money}"
        holder.projectTermino.text = "Término: ${currentContract.expirationDate}"
        holder.projectRestante.text = "${currentContract.tempoRestante}"
        holder.progressBar.progress = currentContract.progressValue

        holder.chatButton.setOnClickListener {
            val intent = Intent(context, UsersActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return manageContractProject.size
    }
}
