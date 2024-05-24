package com.example.rassoonlineapp.Adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Contract
import com.example.rassoonlineapp.Model.Hire
import com.example.rassoonlineapp.Model.ManageContract
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HireAdapter(
    private val mContext: Context,
    private val mHire: MutableList<Hire>,
) : RecyclerView.Adapter<HireAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.hire_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mHire.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val hire = mHire[position]

        // Carrega as informações do usuário
        loadUserData(hire.userId, holder)

        holder.tittle.text = hire.projectName
        holder.description.text = hire.comments
        holder.orcamento.text = hire.editPrice.toString()

        // Formatar e exibir a data/hora
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(hire.timestamp)
        holder.dateHour.text = sdf.format(date)

        // Adiciona o listener para o botão de aceitação
        holder.acceptButton.setOnClickListener {
           showConfirmationDialog(hire, holder.adapterPosition)
        }
        holder.refuseButton.setOnClickListener {
            showConfirmationRefuseDialog(hire, holder.adapterPosition)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_hire)
        var userName: TextView = itemView.findViewById(R.id.user_name_hire)
        var tittle: TextView = itemView.findViewById(R.id.textView_tittle)
        var description: TextView = itemView.findViewById(R.id.textView_description)
        var orcamento: TextView = itemView.findViewById(R.id.textView_preco)
        var deletePost: ImageView = itemView.findViewById(R.id.delete_post)
        var dateHour: TextView = itemView.findViewById(R.id.data_hora)
        val acceptButton: Button = itemView.findViewById(R.id.btn_accept)
        val refuseButton: Button = itemView.findViewById(R.id.btn_refuse)
    }

    private fun loadUserData(userId: String, holder: ViewHolder) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        holder.userName.text = it.getUsername()
                        if (!it.getImage().isNullOrEmpty()) {
                            Picasso.get().load(it.getImage()).placeholder(R.drawable.profile).into(holder.profileImage)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


    private fun showConfirmationDialog(hire: Hire,  position: Int) {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("Confirmação")
        builder.setMessage("Você realmente deseja aceitar este projeto?")

        builder.setPositiveButton("Sim") { dialog, which ->
            createContract(hire)
            removeItem(position)
            deleteHire(hire.hireId)

        }

        builder.setNegativeButton("Não") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showConfirmationRefuseDialog(hire: Hire,  position: Int) {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("Confirmação")
        builder.setMessage("Você realmente deseja recusar este projeto?")

        builder.setPositiveButton("Sim") { dialog, which ->
            deleteHire(hire.hireId)
            removeItem(position)
        }

        builder.setNegativeButton("Não") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteHire(hireId: String) {
        val hireRef = FirebaseDatabase.getInstance().reference.child("hires").child(hireId)
        hireRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Hire deleted successfully
            } else {
                // Failed to delete hire
            }
        }
    }


    private fun removeItem(position: Int) {
        mHire.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mHire.size)
    }

    private fun createContract(hire: Hire) {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("Contracts")
        val manageContractRef = FirebaseDatabase.getInstance().reference.child("ManageContracts")
        val contractId = databaseRef.push().key ?: UUID.randomUUID().toString()
        val manageContractId = manageContractRef.push().key ?: UUID.randomUUID().toString()

        val clientRef = FirebaseDatabase.getInstance().reference.child("Users").child(hire.userId)
        val workerRef = FirebaseDatabase.getInstance().reference.child("Users").child(hire.userIdOther)

        clientRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(clientSnapshot: DataSnapshot) {
                if (clientSnapshot.exists()) {
                    val clientName = clientSnapshot.child("username").getValue(String::class.java) ?: ""

                    workerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(workerSnapshot: DataSnapshot) {
                            if (workerSnapshot.exists()) {
                                val workerName = workerSnapshot.child("username").getValue(String::class.java) ?: ""
                                // Obtém a data e hora atuais para o expirationDate
                                val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                                val contract = Contract(
                                    contractId = contractId,
                                    hireId = hire.hireId,
                                    userId = hire.userId,
                                    userIdOther = hire.userIdOther,
                                    projectName = hire.projectName,
                                    money = hire.editPrice.toString(),
                                    projectDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(hire.timestamp)),
                                    workerName = workerName,
                                    clientName = clientName,
                                    expirationDate = currentDate, // Data e hora do clique
                                    status = "Pending",
                                    description = hire.comments // ou outro status inicial desejado
                                )

                                // Criando o objeto ManageContract com os valores do Contract
                                val manageContract = ManageContract(
                                    manageContractId = manageContractId,
                                    contractId = contractId,
                                    userId = hire.userId,
                                    userIdOther = hire.userIdOther,
                                    projectDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(hire.timestamp)),
                                    expirationDate = currentDate, // Data e hora do clique
                                    tempoRestante = "", // Calcule o tempo restante se necessário
                                    projectName = hire.projectName,
                                    workerName = workerName,
                                    clientName = clientName,
                                    status = "Pending",
                                    money = hire.editPrice.toString(),
                                    description = hire.comments,
                                    progressValue = 0 // ou outro valor inicial desejado
                                )

                                // Salvando o contrato no banco de dados
                                databaseRef.child(contractId).setValue(contract).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Contrato criado com sucesso
                                        // Agora salvando o manageContract no banco de dados
                                        manageContractRef.child(manageContractId).setValue(manageContract).addOnCompleteListener { manageTask ->
                                            if (manageTask.isSuccessful) {
                                                // ManageContract criado com sucesso
                                            } else {
                                                // Falha na criação do ManageContract
                                            }
                                        }
                                    } else {
                                        // Falha na criação do contrato
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle onCancelled
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

}
