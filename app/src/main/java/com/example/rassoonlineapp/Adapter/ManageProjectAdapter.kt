package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.R
import com.google.firebase.database.FirebaseDatabase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

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
        val projectTermino = itemView.findViewById<EditText>(R.id.termino)
        val projectRestante = itemView.findViewById<TextView>(R.id.tempoRestante)
        val editButton = itemView.findViewById<TextView>(R.id.button_edit_text)
        val saveButton = itemView.findViewById<TextView>(R.id.button_save_text)
        val estado = itemView.findViewById<TextView>(R.id.textView_estado)
        val conluidoButton = itemView.findViewById<TextView>(R.id.button_concluido)
        val canceladoButton = itemView.findViewById<TextView>(R.id.button_cancelar)
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
        holder.projectInicialDate.text = "PRAZO: ${currentManageProject.prazo}"
        holder.estado.text = "${currentManageProject.status}"
        holder.projectPay.text = "Pagamento: ${currentManageProject.pay}"
        holder.projectTermino.setText("${currentManageProject.prazoTermino}")
        holder.projectTermino.isEnabled = false

        holder.editButton.setOnClickListener {
            holder.projectTermino.isEnabled = true
        }


        holder.conluidoButton.isEnabled = !currentManageProject.isCancelled
        holder.canceladoButton.isEnabled = !currentManageProject.isCompleted
        holder.conluidoButton.setOnClickListener {
            val databaseReference = FirebaseDatabase.getInstance().reference
            val manageProjectRef = databaseReference.child("ManageProject").child(currentManageProject.manageId)

            if (currentManageProject.isCompleted) {
                Toast.makeText(context, "Este trabalho já foi finalizado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            manageProjectRef.child("status").setValue("Concluído")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Status atualizado para Concluído", Toast.LENGTH_SHORT).show()

                        currentManageProject.isCompleted = true
                        currentManageProject.isCancelled = false

                        holder.conluidoButton.isEnabled = false
                        holder.canceladoButton.isEnabled = true

                        notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        holder.canceladoButton.setOnClickListener {
            val databaseReference = FirebaseDatabase.getInstance().reference
            val manageProjectRef =
                databaseReference.child("ManageProject").child(currentManageProject.manageId)

            if (currentManageProject.isCancelled) {
                Toast.makeText(context, "Este trabalho já foi cancelado.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (currentManageProject.isCompleted) {
                Toast.makeText(context, "Este trabalho já foi finalizado.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            manageProjectRef.child("status").setValue("Cancelado")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Status atualizado para Cancelado",
                            Toast.LENGTH_SHORT
                        ).show()

                        currentManageProject.isCancelled = true
                        currentManageProject.isCompleted = false

                        holder.conluidoButton.isEnabled = true
                        holder.canceladoButton.isEnabled = false

                        notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            context,
                            "Erro ao atualizar o status: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }



        holder.saveButton.setOnClickListener {
            // Extrai os dados editados do campo prazoTermino na interface do usuário
            val novoPrazoTermino = holder.projectTermino.text.toString()

            // Atualiza o objeto ManageProject com as informações editadas
            currentManageProject.prazoTermino = novoPrazoTermino

            // Calcula o tempo restante
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            try {
                val dataInicial = dateFormat.parse(currentManageProject.prazo)
                val dataTermino = dateFormat.parse(novoPrazoTermino)

                if (dataInicial != null && dataTermino != null) {
                    val diff = abs(dataTermino.time - dataInicial.time)
                    val diasRestantes = diff / (1000 * 60 * 60 * 24)

                    currentManageProject.tempoRestante = "Tempo Restante: $diasRestantes dias"
                    holder.projectRestante.text = currentManageProject.tempoRestante

                    // Salvando o tempo restante na base de dados Firebase
                    val databaseReference = FirebaseDatabase.getInstance().reference
                    val manageProjectRef = databaseReference.child("ManageProject").child(currentManageProject.manageId)
                    manageProjectRef.child("tempoRestante").setValue(currentManageProject.tempoRestante)
                } else {
                    currentManageProject.tempoRestante = "Tempo Restante: Data inválida"
                    holder.projectRestante.text = currentManageProject.tempoRestante
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                currentManageProject.tempoRestante = "Tempo Restante: Data inválida"
                holder.projectRestante.text = currentManageProject.tempoRestante
            }

            // Obtém uma referência ao nó correspondente na base de dados Firebase
            val databaseReference = FirebaseDatabase.getInstance().reference
            val manageProjectRef = databaseReference.child("ManageProject").child(currentManageProject.manageId)

            // Atualiza o valor do campo prazoTermino na base de dados Firebase
            manageProjectRef.child("prazoTermino").setValue(novoPrazoTermino)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sucesso ao salvar os dados na base de dados Firebase
                        Toast.makeText(context, "Dados salvos com sucesso", Toast.LENGTH_SHORT).show()
                    } else {
                        // Falha ao salvar os dados na base de dados Firebase
                        Toast.makeText(context, "Erro ao salvar os dados: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        holder.projectRestante.text = currentManageProject.tempoRestante // Define o tempo restante
    }

    override fun getItemCount(): Int {
        return manageProject.size
    }
}