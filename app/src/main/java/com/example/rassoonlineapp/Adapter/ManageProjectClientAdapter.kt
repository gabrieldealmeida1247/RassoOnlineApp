package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.UsersActivity
import com.google.firebase.database.FirebaseDatabase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

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
        val projectClientTermino = itemView.findViewById<EditText>(R.id.client_termino)
        val projectClientRestante = itemView.findViewById<TextView>(R.id.client_tempoRestante)
        val conluidoButton = itemView.findViewById<TextView>(R.id.button_concluido)
        val canceladoButton = itemView.findViewById<TextView>(R.id.button_cancelar)
        val clientStatus = itemView.findViewById<TextView>(R.id.textView_client_status)
        val chatButton = itemView.findViewById<TextView>(R.id.button_chat)
        val editButton = itemView.findViewById<TextView>(R.id.button_edit_text)
        val saveButton = itemView.findViewById<TextView>(R.id.button_save_text)
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
        holder.projectClientTermino.setText("${currentManageProject.prazoTermino}")

        holder.projectClientTermino.isEnabled = false


        // Define o tempo restante
        currentManageProject.tempoRestante = calculateTempoRestante(currentManageProject.prazo, currentManageProject.prazoTermino)
        holder.projectClientRestante.text = currentManageProject.tempoRestante

        // Atualiza o tempo restante no Firebase
        updateTempoRestanteFirebase(currentManageProject.manageId, currentManageProject.tempoRestante)



        holder.chatButton.setOnClickListener {
            val intent = Intent(context, UsersActivity::class.java)
            context.startActivity(intent)
        }


        holder.editButton.setOnClickListener {
            holder.projectClientTermino.isEnabled = true
        }

        holder.saveButton.setOnClickListener {
            updateProjectData(holder, currentManageProject)
        }

        holder.projectClientRestante.text = currentManageProject.tempoRestante // Define o tempo restante

        holder.conluidoButton.setOnClickListener {
            removerPost(currentManageProject)
        }
/*
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

 */

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


    }

    override fun getItemCount(): Int {
        return manageProject.size
    }



    private fun updateProjectData(holder: ManageProjectClientAdapter.ManageProjectViewHolder, currentManageProject: ManageProject) {
        val novoPrazoTermino = holder.projectClientTermino.text.toString()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val dataInicial = dateFormat.parse(currentManageProject.prazo)
            val dataTermino = dateFormat.parse(novoPrazoTermino)

            if (dataInicial != null && dataTermino != null) {
                val diff = abs(dataTermino.time - dataInicial.time)
                val diasRestantes = diff / (1000 * 60 * 60 * 24)

                currentManageProject.tempoRestante = "Tempo Restante: $diasRestantes dias"
                holder.projectClientRestante.text = currentManageProject.tempoRestante

                val databaseReference = FirebaseDatabase.getInstance().reference
                val manageProjectRef = databaseReference.child("ManageProject").child(currentManageProject.manageId)
                manageProjectRef.child("tempoRestante").setValue(currentManageProject.tempoRestante)
            } else {
                currentManageProject.tempoRestante = "Tempo Restante: Data inválida"
                holder.projectClientRestante.text = currentManageProject.tempoRestante
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            currentManageProject.tempoRestante = "Tempo Restante: Data inválida"
            holder.projectClientRestante.text = currentManageProject.tempoRestante
        }

        val databaseReference = FirebaseDatabase.getInstance().reference
        val manageProjectRef = databaseReference.child("ManageProject").child(currentManageProject.manageId)
        manageProjectRef.child("prazoTermino").setValue(novoPrazoTermino)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Dados salvos com sucesso", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Erro ao salvar os dados: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun calculateTempoRestante(prazo: String, prazoTermino: String): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val dataInicial = dateFormat.parse(prazo)
            val dataTermino = dateFormat.parse(prazoTermino)

            if (dataInicial != null && dataTermino != null) {
                val diff = abs(dataTermino.time - dataInicial.time)
                val diasRestantes = diff / (1000 * 60 * 60 * 24)

                return "Tempo Restante: $diasRestantes dias"
            } else {
                return "Tempo Restante: Data inválida"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            return "Tempo Restante: Data inválida"
        }
    }

    private fun updateTempoRestanteFirebase(manageId: String, tempoRestante: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val manageProjectRef = databaseReference.child("ManageProject").child(manageId)
        manageProjectRef.child("tempoRestante").setValue(tempoRestante)
    }


    private fun removerPost(currentManageProject: ManageProject) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val manageProjectRef = databaseReference.child("ManageProject").child(currentManageProject.manageId)

        if (currentManageProject.isCompleted) {
            Toast.makeText(context, "Este trabalho já foi finalizado.", Toast.LENGTH_SHORT).show()
            return
        }

        manageProjectRef.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Remover ManageService relacionado
                    val manageServiceRef = databaseReference.child("ManageService").child(currentManageProject.manageId)
                    manageServiceRef.removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "ManageService removido com sucesso.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Erro ao remover ManageService: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                    // Remover post da lista de posts
                    val postsRef = databaseReference.child("Posts").child(currentManageProject.postId)
                    postsRef.removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Post removido com sucesso.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Erro ao remover post: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                    Toast.makeText(context, "Status atualizado para Concluído", Toast.LENGTH_SHORT).show()

                    currentManageProject.isCompleted = true
                    currentManageProject.isCancelled = false

                    notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

