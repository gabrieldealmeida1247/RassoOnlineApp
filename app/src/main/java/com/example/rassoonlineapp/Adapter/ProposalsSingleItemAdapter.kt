package com.example.rassoonlineapp.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.Model.ManageService
import com.example.rassoonlineapp.Model.ManageServiceHistory
import com.example.rassoonlineapp.Model.Post
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.Model.ProposalsStatistic
import com.example.rassoonlineapp.Model.Rating
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProposalsSingleItemAdapter(private var proposalsList: List<Proposals>) : RecyclerView.Adapter<ProposalsSingleItemAdapter.ViewHolder>() {
    private val processedProposals: MutableSet<String> = mutableSetOf()
    private lateinit var databaseReference: DatabaseReference
   // conjunto para armazenar IDs de propostas processadas


    interface ProposalAcceptListener {
        fun onProposalAccepted(proposal: Proposals)
        fun onProposalRejected(proposal: Proposals)
    }


    private var firebaseUser: FirebaseUser? = null

    private var acceptListener: ProposalAcceptListener? = null
    private var rejectedListener: ProposalAcceptListener? = null

    fun setRejectedListener(listener: ProposalAcceptListener) {
        this.rejectedListener = listener
    }


    fun setAcceptListener(listener: ProposalAcceptListener) {
        this.acceptListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.proposals_receive_item_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Aqui você pode definir os dados para os elementos do layout, se necessário

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val proposal = proposalsList[position]

        // Carrega as informações do usuário no PostAdapter
        loadProposalData(proposal.userId.toString(), proposal, holder)
        // Calcula e salva a quantidade de propostas recebidas
        saveReceivedProposalsCountToFirebase()

        holder.descricaoTextView.text = proposal.descricao
        holder.lanceTextView.text = proposal.lance
        // Formatar a data de numberDays para exibir no TextView
      holder.numberDay.text = proposal.numberDays
        holder.tittle.text = proposal.projectTitle // Exibe


        holder.buttonAceitar.setOnClickListener {
            // Removendo listeners para evitar chamadas múltiplas
  //          holder.buttonAceitar.setOnClickListener(null)
//            holder.buttonRecusado.setOnClickListener(null)

            acceptListener?.onProposalAccepted(proposalsList[position])
        }

        holder.buttonRecusado.setOnClickListener {
            // Removendo listeners para evitar chamadas múltiplas
            //holder.buttonAceitar.setOnClickListener(null)
          //  holder.buttonRecusado.setOnClickListener(null)

            rejectedListener?.onProposalRejected(proposalsList[position])
        }

        if (proposal.accepted != "Aprovado" && proposal.rejected != "Reprovado") {
            holder.itemView.visibility = View.VISIBLE
        } else {
            holder.itemView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        // Neste caso, estamos exibindo apenas um item
        return proposalsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Aqui você pode inicializar os elementos do layout, se necessário

        val userProfileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val descricaoTextView: TextView = itemView.findViewById(R.id.textView_description_proposals)
        val lanceTextView: TextView = itemView.findViewById(R.id.textView_bid)
        val numberDay: TextView = itemView.findViewById(R.id.textView_number_day)
        val textView_rating: TextView = itemView.findViewById(R.id.number_rating)
        val tittle: TextView = itemView.findViewById(R.id.textView_titulo_propostas)
        val buttonAceitar: Button = itemView.findViewById(R.id.botao_aceitar)
        val buttonRecusado: Button = itemView.findViewById(R.id.botao_recusado)

        init {
            buttonAceitar.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val proposal = proposalsList[position]
                    acceptListener?.onProposalAccepted(proposal)
                }
            }
        }

        init {
            buttonRecusado.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val proposal = proposalsList[position]
                    rejectedListener?.onProposalRejected(proposal)
                }
            }
        }
    }


    fun loadProposalData(userId: String, proposals: Proposals, holder: ViewHolder) {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    // Adiciona o nome de usuário e a imagem do usuário ao objeto Post
                    proposals.username = user?.getUsername()
                    proposals. profileImage = user?.getImage()

                    // Carrega o rating do usuário no TextView
                    loadUserRating(userId, holder)

                    // Verifica se o URL da imagem não é nulo ou vazio antes de carregá-lo
                    // Verifica se o URL da imagem não é nulo ou vazio antes de carregá-lo
                    val userProfileImage = user?.getImage()
                    if (!userProfileImage.isNullOrBlank()) {
                        Picasso.get().load(userProfileImage).placeholder(R.drawable.profile)
                            .into(holder.userProfileImage)
                    }

                    holder.userName.text = user?.getUsername()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    internal fun createManageService(proposal: Proposals) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("ManageService").child(proposal.proposalId!!)

        val manageServiceId = proposal.proposalId // Usando proposalId como a chave

        // Obtendo o nome do usuário atualmente autenticado a partir do banco de dados Users
        currentUser?.uid?.let { uid ->
            val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    val clientName = user?.getUsername() ?: ""
                    // Criando o objeto ManageService com os dados obtidos
                    // Formatar createdAt para exibir apenas a hora
                    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                    val manageService = ManageService(
                        serviceId = manageServiceId!!,
                        proposalId = proposal.proposalId!!,
                        userId = proposal.userId!!,
                        userIdOther = proposal.userIdOther!!,
                        status = "ativo",
                        money = proposal.lance!!,
                        projectDate = proposal.numberDays.toString(),
                        workerName = proposal.username ?: "", // Nome de quem enviou a proposta
                        clientName = clientName, // Nome do usuário autenticado
                        projectName = proposal.projectTitle!!,
                        expirationDate = proposal.numberDays,
                        createdAt = currentTime
                    )

                    // Salvando o objeto ManageService no banco de dados
                    databaseReference.setValue(manageService)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Sucesso ao criar o ManageService
                            } else {
                                // Falha ao criar o ManageService
                            }
                        }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }
    }

    internal fun createManageProject(proposal: Proposals) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ManageProject").child(proposal.proposalId!!)

        val manageProjectId = proposal.proposalId // Usando proposalId como a chave

        // Buscando workerName e clientName da base de dados ManageService
        val manageServiceRef = FirebaseDatabase.getInstance().reference.child("ManageService").child(proposal.proposalId!!)
        manageServiceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(manageServiceSnapshot: DataSnapshot) {
                val manageService = manageServiceSnapshot.getValue(ManageService::class.java)

                // Obtendo a descrição e habilidades do Post
                val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(proposal.postId!!)
                postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(postSnapshot: DataSnapshot) {
                        val post = postSnapshot.getValue(Post::class.java)

                        // Criando o objeto ManageProject com os dados obtidos
                        val manageProject = ManageProject(
                            manageId = manageProjectId!!,
                            serviceId = proposal.serviceId ?: "",
                            proposalId = proposal.proposalId!!,
                            userId = proposal.userId!!,
                            postId = proposal.postId ?: "",
                            projectName = proposal.projectTitle ?: "",
                            description = post?.descricao ?: "", // Usando a descrição do Post
                            skills = post?.habilidades ?: emptyList(), // Usando as habilidades do Post
                            workerName = manageService?.workerName ?: "", // Usando workerName do ManageService
                            clientName = manageService?.clientName ?: "", // Usando clientName do ManageService
                            prazo = proposal.prazoAceitacao ?: "", // Defina o prazo conforme necessário
                            prazoTermino = proposal.numberDays.toString(), // Defina o prazo de término conforme necessário
                            pay = manageService?.money ?: "", // Defina o pagamento conforme necessário
                            status = "ativo", // Defina o status conforme necessário
                            tempoRestante = ""
                        )

                        // Salvando o objeto ManageProject no banco de dados
                        databaseReference.setValue(manageProject)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Sucesso ao criar o ManageProject
                                } else {
                                    // Falha ao criar o ManageProject
                                }
                            }
                    }

                    override fun onCancelled(postDatabaseError: DatabaseError) {
                        // Handle onCancelled
                    }
                })
            }

            override fun onCancelled(manageServiceDatabaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    internal fun createManageServiceHistory(proposal: Proposals) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ManageServiceHistory").child(proposal.proposalId!!)

        val serviceHistoryId = proposal.proposalId // Usando proposalId como a chave

        // Buscando workerName e clientName da base de dados ManageService
        val manageServiceProjectRef = FirebaseDatabase.getInstance().reference.child("ManageProject").child(proposal.proposalId!!)
        manageServiceProjectRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(manageServiceProjectSnapshot: DataSnapshot) {
                val manageServiceProject = manageServiceProjectSnapshot.getValue(ManageProject::class.java)


                // Buscando workerName e clientName da base de dados ManageService
                val manageServiceRef = FirebaseDatabase.getInstance().reference.child("ManageService").child(proposal.proposalId!!)
                manageServiceRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(manageServiceSnapshot: DataSnapshot) {
                        val manageService = manageServiceSnapshot.getValue(ManageService::class.java)

                        // Obtendo a descrição e habilidades do Post
                        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(proposal.postId!!)
                        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(postSnapshot: DataSnapshot) {
                                val post = postSnapshot.getValue(Post::class.java)

                                // Criando o objeto ManageProject com os dados obtidos
                                val manageSeviceHistory = ManageServiceHistory(
                                    serviceHistoryId = serviceHistoryId!!,
                                    proposalId = proposal.proposalId!!,
                                    userId = proposal.userId!!,
                                    postId = proposal.postId ?: "",
                                    projectName = proposal.projectTitle ?: "",
                                    money = manageService?.money?: "",
                                    projectDate = manageService?.projectDate?: "",
                                    status = "",
                                    userIdOther = proposal?.userIdOther?: "",
                                    workerName = manageService?.workerName ?: "", // Usando workerName do ManageService
                                    clientName = manageService?.clientName ?: "", // Usando clientName do ManageService
                                    expirationDate = manageService?.expirationDate ?: "",
                                    createdAt = manageService?.createdAt ?: "",// Defina o prazo conforme necessário

                                )

                                // Salvando o objeto ManageProject no banco de dados
                                databaseReference.setValue(manageSeviceHistory)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Sucesso ao criar o ManageProject
                                        } else {
                                            // Falha ao criar o ManageProject
                                        }
                                    }
                            }

                            override fun onCancelled(postDatabaseError: DatabaseError) {
                                // Handle onCancelled
                            }
                        })
                    }

                    override fun onCancelled(manageServiceHistoryDatabaseError: DatabaseError) {
                        // Handle onCancelled
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    fun calculateReceivedProposals(): Int {
        var receivedCount = 0
        for (proposal in proposalsList) {
            if (proposal.accepted != "Aprovado" && proposal.rejected != "Reprovado") {
                receivedCount++
            }
        }
        return receivedCount
    }

    fun saveReceivedProposalsCountToFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        // Referência para o nó ProposalsStatistic
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ProposalStats").child(userId)

        // Recuperando ou criando ProposalsStatistic para o usuário
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val statistic = if (dataSnapshot.exists()) {
                    dataSnapshot.getValue(ProposalsStatistic::class.java)
                } else {
                    ProposalsStatistic(userId = userId)
                } ?: ProposalsStatistic(userId = userId)

                // Atualizando a quantidade de propostas recebidas
                val receivedCount = calculateReceivedProposals()
                statistic.proposalsReceiveCount = receivedCount

                // Salvando o objeto atualizado no banco de dados
                databaseReference.setValue(statistic)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sucesso ao salvar os dados
                        } else {
                            // Falha ao salvar os dados
                        }
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


    fun updateData(newList: List<Proposals>) {
        proposalsList = newList.toMutableList()
        notifyDataSetChanged()
    }


    private fun loadUserRating(userId: String,  holder: ViewHolder) {
        val ratingsRef = FirebaseDatabase.getInstance().reference.child("Ratings").child(userId)

        ratingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val rating = dataSnapshot.getValue(Rating::class.java)
                    val ratingId = rating?.ratingId ?: ""
                    Log.d("loadUserRating", "RatingId: $ratingId")

                    holder.textView_rating.text = rating?.rating.toString()
                } else {
                    holder.textView_rating.text = "0.1"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("loadUserRating", "Erro ao recuperar rating", databaseError.toException())
            }
        })
    }

}