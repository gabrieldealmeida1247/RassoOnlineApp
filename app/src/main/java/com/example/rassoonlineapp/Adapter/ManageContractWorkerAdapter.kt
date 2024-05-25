package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageContract
import com.example.rassoonlineapp.Model.ServiceContractCount
import com.example.rassoonlineapp.Model.StatisticContract
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.UsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageContractWorkerAdapter(private val context: Context, private val manageContractProject: List<ManageContract>) :
    RecyclerView.Adapter<ManageContractWorkerAdapter.ManageContractViewHolder>() {

    private val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    inner class ManageContractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        val cancelButton: ImageView = itemView.findViewById(R.id.button_cancelar_contract)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageContractViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manage_contract_worker_item_layout, parent, false)
        return ManageContractViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageContractViewHolder, position: Int) {
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

        holder.cancelButton.setOnClickListener {
            showConfirmationDialogCancel(currentContract)
        }

        holder.chatButton.setOnClickListener {
            val intent = Intent(context, UsersActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return manageContractProject.size
    }

    private fun showConfirmationDialogCancel(currentContract: ManageContract) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmar Cancelamento")
        builder.setMessage("Deseja realmente marcar este projeto como cancelado?")
        builder.setPositiveButton("Sim") { _, _ ->
            handleCancelButtonClick(currentContract)
        }
        builder.setNegativeButton("Não") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
/*
    private fun handleCancelButtonClick(currentContract: ManageContract) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val manageProjectRef = databaseReference.child("ManageContracts").child(currentContract.manageContractId)

        if (currentContract.isCancelled) {
            Toast.makeText(context, "Este trabalho já foi cancelado.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentContract.isCompleted) {
            Toast.makeText(context, "Este trabalho já foi finalizado.", Toast.LENGTH_SHORT).show()
            return
        }

        manageProjectRef.child("status").setValue("Cancelado")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentContract.status = "Cancelado"
                    ManageCancelCount(currentContract.userId)
                    // Incrementar o contador de serviços cancelados
                    val statisticRef = databaseReference.child("StatisticContract").child(firebaseUser!!.uid)

                    statisticRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val statistic = dataSnapshot.getValue(StatisticContract::class.java)

                            if (statistic != null) {
                                val updatedServiceCancel = statistic.serviceCancel + 1
                                statisticRef.child("serviceCancel").setValue(updatedServiceCancel)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Continue com as ações após a atualização bem-sucedida
                                            notifyDataSetChanged()
                                            Toast.makeText(context, "Status atualizado para Cancelado", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle onCancelled
                        }
                    })

                    // Continue com as outras ações após a atualização do status
                    loadUserData(firebaseUser!!.uid) { userName, userProfileImage ->
                        // Enviar uma notificação para o usuário que fez a proposta
                        // addNotificationCancel(currentManageProject.userId ?: "", currentManageProject.postId ?: "", userName, userProfileImage, currentManageProject.projectName ?: "")

                        // Atualize os detalhes do usuário que fez a proposta com os detalhes do usuário que aceitou a proposta
                        updateProposerUserDetails(currentContract.userId ?: "", userName, userProfileImage)

                        notifyDataSetChanged()
                    }

                    Toast.makeText(context, "Status atualizado para Cancelado", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

 */

    private fun handleCancelButtonClick(currentContract: ManageContract) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val manageProjectRef = databaseReference.child("ManageContracts").child(currentContract.manageContractId)

        if (currentContract.isCancelled) {
            Toast.makeText(context, "Este trabalho já foi cancelado.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentContract.isCompleted) {
            Toast.makeText(context, "Este trabalho já foi finalizado.", Toast.LENGTH_SHORT).show()
            return
        }

        manageProjectRef.child("status").setValue("Cancelado")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentContract.status = "Cancelado"
                    ManageCancelCount(currentContract.userId)

                    val statisticRef = databaseReference.child("StatisticContract").child(firebaseUser!!.uid)

                    statisticRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val statistic = dataSnapshot.getValue(StatisticContract::class.java)
                            val updatedServiceCancel = (statistic?.serviceCancel ?: 0) + 1

                            statisticRef.child("serviceCancel").setValue(updatedServiceCancel)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        notifyDataSetChanged()
                                        Toast.makeText(context, "Status atualizado para Cancelado", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Erro ao acessar os dados de estatísticas: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })

                    loadUserData(firebaseUser!!.uid) { userName, userProfileImage ->
                        updateProposerUserDetails(currentContract.userId ?: "", userName, userProfileImage)
                        notifyDataSetChanged()
                    }

                    Toast.makeText(context, "Status atualizado para Cancelado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun loadUserData(userId: String, callback: (String, String?) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    val userName = user?.getUsername() ?: ""
                    val userProfileImage = user?.getImage()

                    callback(userName, userProfileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


    private fun updateProposerUserDetails(userId: String, userName: String, userProfileImage: String?) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        val userUpdates = hashMapOf<String, Any>()
        userUpdates["userName"] = userName
        userUpdates["userProfileImage"] = userProfileImage!!


        userRef.updateChildren(userUpdates)
            .addOnSuccessListener {
                Log.d("ProposalsFragment", "Detalhes do usuário atualizados com sucesso.")
            }
            .addOnFailureListener { e ->
                Log.e("ProposalsFragment", "Erro ao atualizar os detalhes do usuário.", e)
            }
    }
    private fun ManageCancelCount(userId: String) {
        val postRef = FirebaseDatabase.getInstance().reference.child("ServiceContractCount").child(userId)
        postRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val cancelCount = dataSnapshot.child("cancelCount").getValue(Int::class.java) ?: 0
                postRef.child("cancelCount").setValue(cancelCount + 1)
            } else {
                val serviceContractCount = ServiceContractCount(cancelCount = 1, concludeCount = 0)
                postRef.setValue(serviceContractCount)
            }
        }
    }



}
