package com.example.rassoonlineapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.Model.ManageService
import com.example.rassoonlineapp.Model.Post
import com.example.rassoonlineapp.Model.Proposals
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

class ProposalsSingleItemAdapter(private val proposalsList: List<Proposals>) : RecyclerView.Adapter<ProposalsSingleItemAdapter.ViewHolder>() {
    private lateinit var databaseReference: DatabaseReference

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

        holder.descricaoTextView.text = proposal.descricao
        holder.lanceTextView.text = proposal.lance
        holder.numberDay.text = proposal.numberDays
        holder.tittle.text = proposal.projectTitle // Exibe

        holder.buttonAceitar.setOnClickListener {
            acceptListener?.onProposalAccepted(proposalsList[position])
        }

        holder.buttonRecusado.setOnClickListener {
            rejectedListener?.onProposalRejected(proposalsList[position])
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
                    val manageService = ManageService(
                        serviceId = manageServiceId!!,
                        proposalId = proposal.proposalId!!,
                        userId = proposal.userId!!,
                        status = "ativo",
                        money = proposal.lance!!,
                        projectDate = proposal.numberDays!!,
                        workerName = proposal.username ?: "", // Nome de quem enviou a proposta
                        clientName = clientName, // Nome do usuário autenticado
                        projectName = proposal.projectTitle!!,
                        expirationDate = "22 de Maio"
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
                            serviceId = manageService?.serviceId ?: "",
                            proposalId = proposal.proposalId!!,
                            userId = proposal.userId!!,
                            postId = proposal.postId ?: "",
                            projectName = proposal.projectTitle ?: "",
                            description = post?.descricao ?: "", // Usando a descrição do Post
                            skills = post?.habilidades ?: emptyList(), // Usando as habilidades do Post
                            workerName = manageService?.workerName ?: "", // Usando workerName do ManageService
                            clientName = manageService?.clientName ?: "", // Usando clientName do ManageService
                            prazo = proposal.prazoAceitacao ?: "", // Defina o prazo conforme necessário
                            prazoTermino = post?.prazo ?: "", // Defina o prazo de término conforme necessário
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

}