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
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Admin.model.ServiceCount
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.Model.Statistic
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.UsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageProjectAdapter(private val context: Context, private val manageProject: List<ManageProject>) :
    RecyclerView.Adapter<ManageProjectAdapter.ManageProjectViewHolder>() {

    private val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

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
        val chatButton = itemView.findViewById<ImageView>(R.id.button_chat)
        val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
        val cancelButton = itemView.findViewById<ImageView>(R.id.button_cancelar)
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
        holder.projectTermino.text = "Término: ${currentManageProject.prazoTermino}"
        holder.projectRestante.text = "${currentManageProject.tempoRestante}"
        holder.progressBar.progress = currentManageProject.progressValue

holder.cancelButton.setOnClickListener {
    handleCancelButtonClick(currentManageProject)
}

        holder.chatButton.setOnClickListener {
            val intent = Intent(context, UsersActivity::class.java)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return manageProject.size
    }


    private fun handleCancelButtonClick(currentManageProject: ManageProject) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val manageProjectRef = databaseReference.child("ManageProject").child(currentManageProject.manageId)

        if (currentManageProject.isCancelled) {
            Toast.makeText(context, "Este trabalho já foi cancelado.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentManageProject.isCompleted) {
            Toast.makeText(context, "Este trabalho já foi finalizado.", Toast.LENGTH_SHORT).show()
            return
        }

        manageProjectRef.child("status").setValue("Cancelado")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentManageProject.status = "Cancelado"
                    ManageCancelCount()
                    // Incrementar o contador de serviços cancelados
                    val statisticRef = databaseReference.child("Statistics").child(firebaseUser!!.uid)

                    statisticRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val statistic = dataSnapshot.getValue(Statistic::class.java)

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
                        addNotificationCancel(currentManageProject.userId ?: "", currentManageProject.postId ?: "", userName, userProfileImage, currentManageProject.projectName ?: "")

                        // Atualize os detalhes do usuário que fez a proposta com os detalhes do usuário que aceitou a proposta
                        updateProposerUserDetails(currentManageProject.userId ?: "", userName, userProfileImage)

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
    private fun addNotificationCancel(userId: String, postId: String, userName: String, userProfileImage: String?, projectName: String) {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(userId)
        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = firebaseUser!!.uid
        notiMap["postTitle"] = "Cancelou o serviço:$projectName"
        notiMap["postId"] = postId
        notiMap["ispost"] = true
        notiMap["userName"] = userName
        notiMap["userProfileImage"] = userProfileImage ?: ""

        notiRef.push().setValue(notiMap)
    }
    private fun ManageCancelCount(){
        val postRef = FirebaseDatabase.getInstance().reference.child("ServiceCount")
        postRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val statistic = dataSnapshot.getValue(ServiceCount::class.java)
                statistic?.let {
                    val cancelCount = it.cancelCount + 1
                    it.cancelCount = cancelCount
                    postRef.setValue(it)
                }
            } else {
                val service = ServiceCount(concludeCount = 0, cancelCount = 1,postsCount = 0, propCount = 0,
                    proposalsRefuseCount = 0, proposalsAcceptCount = 0)
                postRef.setValue(service)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao obter os dados das estatísticas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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

}