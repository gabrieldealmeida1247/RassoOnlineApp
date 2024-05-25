package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Admin.model.ServiceCount
import com.example.rassoonlineapp.Fragments.PaymentFragment
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
import de.hdodenhof.circleimageview.CircleImageView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs




class ManageProjectClientAdapter(private val context: Context, private val manageProject: List<ManageProject>) :
    RecyclerView.Adapter<ManageProjectClientAdapter.ManageProjectViewHolder>() {

    private val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

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
        val conluidoButton = itemView.findViewById<ImageView>(R.id.button_concluido)
        val canceladoButton = itemView.findViewById<ImageView>(R.id.button_cancelar)
        val clientStatus = itemView.findViewById<TextView>(R.id.textView_client_status)
        val chatButton = itemView.findViewById<ImageView>(R.id.button_chat)
        val editButton = itemView.findViewById<ImageView>(R.id.button_edit_text)
        val saveButton = itemView.findViewById<ImageView>(R.id.button_save_text)
        val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
        val imageWorker = itemView.findViewById<CircleImageView>(R.id.imageWorker)
        val imageClient = itemView.findViewById<CircleImageView>(R.id.imageClient)
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
        holder.projectInicialDate.text = "Início: ${currentManageProject.prazo}"
        holder.clientStatus.text = "${currentManageProject.status}"
        holder.projectPay.text = "Pagamento: ${currentManageProject.pay}"
        holder.projectClientTermino.setText("${currentManageProject.prazoTermino}")


        holder.projectClientTermino.isEnabled = false

        val (tempoRestante, progresso) = calculateTempoRestante(currentManageProject.prazo, currentManageProject.prazoTermino)
        currentManageProject.tempoRestante = tempoRestante
        holder.projectClientRestante.text = currentManageProject.tempoRestante
        holder.progressBar.progress = progresso

        // Atualiza o tempo restante no Firebase
        updateTempoRestanteFirebase(currentManageProject.manageId, currentManageProject.tempoRestante, progresso)



        holder.chatButton.setOnClickListener {
            val intent = Intent(context, UsersActivity::class.java)
            context.startActivity(intent)
        }


        holder.editButton.setOnClickListener {
            holder.projectClientTermino.isEnabled = true
        }

        holder.saveButton.setOnClickListener {
            updateProjectData(holder, currentManageProject)
            holder.projectClientTermino.isEnabled = false
        }

        holder.projectClientRestante.text = currentManageProject.tempoRestante // Define o tempo restante

        holder.conluidoButton.setOnClickListener {
          showConfirmationDialog(currentManageProject)
        }

        holder.canceladoButton.setOnClickListener {
            handleCancelButtonClick(currentManageProject)

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

                val (tempoRestante, progresso) = calculateTempoRestante(currentManageProject.prazo, novoPrazoTermino)
                currentManageProject.tempoRestante = tempoRestante
                holder.projectClientRestante.text = tempoRestante
                holder.progressBar.progress = progresso

                val databaseReference = FirebaseDatabase.getInstance().reference
                val manageProjectRef = databaseReference.child("ManageProject").child(currentManageProject.manageId)
                manageProjectRef.child("tempoRestante").setValue(currentManageProject.tempoRestante)
                manageProjectRef.child("prazoTermino").setValue(novoPrazoTermino)
                manageProjectRef.child("progressValue").setValue(progresso)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Dados salvos com sucesso", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Erro ao salvar os dados: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                currentManageProject.tempoRestante = "Tempo Restante: Data inválida"
                holder.projectClientRestante.text = currentManageProject.tempoRestante
                holder.progressBar.progress = 0
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            currentManageProject.tempoRestante = "Tempo Restante: Data inválida"
            holder.projectClientRestante.text = currentManageProject.tempoRestante
            holder.progressBar.progress = 0
        }
    }

     private fun calculateTempoRestante(prazo: String, prazoTermino: String): Pair<String, Int> {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val dataInicial = dateFormat.parse(prazo)
            val dataTermino = dateFormat.parse(prazoTermino)

            if (dataInicial != null && dataTermino != null) {
                val totalDias = (dataTermino.time - dataInicial.time) / (1000 * 60 * 60 * 24)
                val diasRestantes = (dataTermino.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)

                // Ajuste para lidar com o caso em que os dias restantes são negativos
                val diasRestantesAjustados = if (diasRestantes < 0) 0 else diasRestantes.toInt()

                // Verifica se as datas são iguais e ajusta o progresso
                val progresso = if (totalDias <= 0) {
                    100
                } else {
                    ((totalDias - diasRestantesAjustados).toDouble() / totalDias * 100).toInt()
                }

                "Tempo Restante: $diasRestantesAjustados dias" to progresso
            } else {
                "Tempo Restante: Data inválida" to 0
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            "Tempo Restante: Data inválida" to 0
        }
    }

    private fun updateTempoRestanteFirebase(manageId: String, tempoRestante: String, progressValue: Int) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val manageProjectRef = databaseReference.child("ManageProject").child(manageId)
        manageProjectRef.child("tempoRestante").setValue(tempoRestante)
        manageProjectRef.child("progressValue").setValue(progressValue)
    }

    private fun removerPost(currentManageProject: ManageProject) {
        val databaseReference = FirebaseDatabase.getInstance().reference

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
// Notificação de proposta rescusada

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

    // Dentro da classe ManageProjectClientAdapter
    private fun showConfirmationDialog(currentManageProject: ManageProject) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmar Conclusão")
        builder.setMessage("Deseja realmente marcar este projeto como concluído?")
        builder.setPositiveButton("Sim") { _, _ ->
            navigateToPaymentDialog(currentManageProject)
        }
        builder.setNegativeButton("Não") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun navigateToPaymentDialog(currentManageProject: ManageProject) {
        val activity = context as FragmentActivity
        val paymentFragment = PaymentFragment()

        val bundle = Bundle().apply {
            putString("manageId", currentManageProject.manageId)
            putString("userId", currentManageProject.userId)
            putString("workerName", currentManageProject.workerName)
        }

        paymentFragment.arguments = bundle

        // Use show para exibir o DialogFragment
        paymentFragment.show(activity.supportFragmentManager, "PaymentFragment")
    }




    private fun handleConcluidoButtonClick(currentManageProject: ManageProject) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val manageProjectRef = databaseReference.child("ManageProject").child(currentManageProject.manageId)

        manageProjectRef.child("status").setValue("Concluído")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentManageProject.status = "Concluído"
                    ManageCount()

                    // Incrementar o contador de serviços concluídos
                    val statisticRef = databaseReference.child("Statistics").child(firebaseUser!!.uid)

                    statisticRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val statistic = dataSnapshot.getValue(Statistic::class.java)

                            if (statistic != null) {
                                val updatedServiceConclude = statistic.serviceConclude + 1
                                statisticRef.child("serviceConclude").setValue(updatedServiceConclude)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Continue com as ações após a atualização bem-sucedida
                                            notifyDataSetChanged()
                                            Toast.makeText(context, "Status atualizado para Concluído", Toast.LENGTH_SHORT).show()
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
                        addNotification(currentManageProject.userId ?: "", currentManageProject.postId ?: "", userName, userProfileImage, currentManageProject.projectName ?: "")

                        // Atualize os detalhes do usuário que fez a proposta com os detalhes do usuário que aceitou a proposta
                        updateProposerUserDetails(currentManageProject.userId ?: "", userName, userProfileImage)

                        notifyDataSetChanged()
                    }

                    Toast.makeText(context, "Status atualizado para Concluído", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun ManageCount(){
        val postRef = FirebaseDatabase.getInstance().reference.child("ServiceCount")
        postRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val statistic = dataSnapshot.getValue(ServiceCount::class.java)
                statistic?.let {
                    val concludeCount = it.concludeCount + 1
                    it.concludeCount = concludeCount
                    postRef.setValue(it)
                }
            } else {
                val service = ServiceCount(concludeCount = 1, cancelCount = 0,postsCount = 0, propCount = 0,
                    proposalsRefuseCount = 0, proposalsAcceptCount = 0, deleteCount = 0)
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

    private fun addNotification(userId: String, postId: String, userName: String, userProfileImage: String?, projectName: String) {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(userId)
        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = firebaseUser!!.uid
        notiMap["postTitle"] = "Concluio o serviço:$projectName"
        notiMap["postId"] = postId
        notiMap["ispost"] = true
        notiMap["userName"] = userName
        notiMap["userProfileImage"] = userProfileImage ?: ""

        notiRef.push().setValue(notiMap)
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
                    proposalsRefuseCount = 0, proposalsAcceptCount = 0, deleteCount = 0)
                postRef.setValue(service)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao obter os dados das estatísticas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

