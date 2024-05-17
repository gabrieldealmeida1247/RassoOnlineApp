package com.example.rassoonlineapp.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.PostAdapter
import com.example.rassoonlineapp.Adapter.ProposalAdapter
import com.example.rassoonlineapp.Adapter.ProposalsSingleItemAdapter
import com.example.rassoonlineapp.Adapter.ProposalsStatisticAdapter
import com.example.rassoonlineapp.Admin.model.ServiceCount
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.Model.ManageService
import com.example.rassoonlineapp.Model.ManageServiceHistory
import com.example.rassoonlineapp.Model.Post
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.Model.ProposalsStatistic
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.Utils.NetworkChangeReceiver
import com.example.rassoonlineapp.ViewModel.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class ProposalsFragment : Fragment() {
    private var proposalsAdapter: ProposalAdapter? = null
    private var proposalList: MutableList<Proposals>? = null
    private var firebaseUser: FirebaseUser? = null
    private var proposalsRef: DatabaseReference? = null
    private lateinit var sharedViewModel: SharedViewModel
    private var proposalsSingleItemAdapter: ProposalsSingleItemAdapter? = null
    private var progressBar: ProgressBar? = null
    private val handler = android.os.Handler()
    private val networkChangeReceiver = NetworkChangeReceiver()
    private lateinit var postAdapter: PostAdapter
    private lateinit var proposalsStatistic: ProposalsStatisticAdapter
    private val  proposalsStatisticList: MutableList<ProposalsStatistic> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_proposals, container, false)
        val viewSwitcher = view.findViewById<ViewSwitcher>(R.id.view_switcher_proposols)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        progressBar = view.findViewById(R.id.progress_bar)
        // Inicializa o postAdapter aqui, após a criação do RecyclerView
        postAdapter = PostAdapter(requireContext(), listOf()) // Você pode passar uma lista vazia ou os dados necessários
        viewSwitcher.post { viewSwitcher.setDisplayedChild(0) }

        view.findViewById<Button>(R.id.button_propostas).setOnClickListener {
            // Lógica para exibir o layout principal no ViewSwitcher
            viewSwitcher.setDisplayedChild(0)
            hidePortfolioRecyclerView()
            showRatingElements()
        }

        view.findViewById<Button>(R.id.button_propostas_aceitar).setOnClickListener {
            // Lógica para exibir o layout de portfólio no ViewSwitcher
            viewSwitcher.setDisplayedChild(1)
            showPortfolioRecyclerView()
            hideRatingElements()
            // Inflar o layout do item de portfólio diretamente na RecyclerView
            val recyclerViewProposolsReceive = view.findViewById<RecyclerView>(R.id.recycler_view_proposals_receive)
            recyclerViewProposolsReceive.layoutManager = LinearLayoutManager(context) // Adicione um gerenciador de layout se necessário

            // Filtra a lista de propostas para exibir apenas as propostas de outros usuários
            val otherUserProposals = proposalList?.filter { it.userId != firebaseUser?.uid && it.userIdOther == firebaseUser?.uid }
            val proposalsAdapter = ProposalsSingleItemAdapter(otherUserProposals ?: listOf())

            proposalsAdapter.setAcceptListener(object :
                ProposalsSingleItemAdapter.ProposalAcceptListener {
                override fun onProposalAccepted(proposal: Proposals) {
                    acceptProposal(proposal)
                    createManageService(proposal)
                    createManageProject(proposal)
                 //   createManageService(proposal)
                  //  updateProposalCountInStatistic(true)
                }

                override fun onProposalRejected(proposal: Proposals) {
                    // Aqui você implementa a lógica para rejeitar a proposta
                    // Por exemplo, você pode atualizar o status da proposta no banco de dados
                    // ou realizar outras ações necessárias

                    // Exemplo de implementação:
                    // rejectedProposal(proposal)
                    // ou
                    // showToast("Proposta rejeitada")
                }
            })

            proposalsAdapter.setRejectedListener(object :
                ProposalsSingleItemAdapter.ProposalAcceptListener {
                override fun onProposalAccepted(proposal: Proposals) {
                    // Aqui você implementa a lógica para aceitar a proposta
                    // Por exemplo, você pode atualizar o status da proposta no banco de dados
                    // ou realizar outras ações necessárias

                    // Exemplo de implementação:
                    // acceptProposal(proposal)
                    // ou
                    // showToast("Proposta aceita")
                }

                override fun onProposalRejected(proposal: Proposals) {
                    rejectedProposal(proposal)
                }
            })

            recyclerViewProposolsReceive.adapter = proposalsAdapter // Aqui você define o adaptador
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        proposalsRef = FirebaseDatabase.getInstance().reference.child("Proposals")

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_proposals)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        proposalList = ArrayList()
        proposalsAdapter = ProposalAdapter(requireContext(), proposalList as ArrayList<Proposals>)
        // Corrigido para usar ProposalAdapter
        recyclerView.adapter = proposalsAdapter // Corrigido para usar ProposalAdapter

        retrieveProposals()

        return view
    }

    private fun retrieveProposals() {
        if (!checkInternetConnection()) {
            // Mostrar Snackbar de erro
            val snackbar = Snackbar.make(
                requireView(),
                "Por favor, conecte-se à internet para atualizar os dados.",
                Snackbar.LENGTH_LONG
            )
            snackbar.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red)) // Define a cor de fundo do Snackbar
            snackbar.show()

            // Agendar a recuperação dos dados após um atraso
            scheduleDataRetrieval()
            return
        }
        progressBar?.visibility = View.VISIBLE // Mostra o ProgressBar
        proposalsRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                proposalList?.clear()
                for (proposalSnapshot in snapshot.children) {
                    val proposal = proposalSnapshot.getValue(Proposals::class.java)
                    proposal?.let {
                        proposalList?.add(it)
                    }
                }

                // Adicione um log para verificar as propostas recebidas
                proposalList?.forEach { proposal ->
                    Log.d("ProposalsFragment", "Proposal: ${proposal.proposalId}, User ID: ${proposal.userId}")
                }

                proposalsAdapter?.notifyDataSetChanged()
                progressBar?.visibility = View.GONE // Esconde o ProgressBar após carregar os dados

            }

            override fun onCancelled(error: DatabaseError) {
                progressBar?.visibility = View.GONE // Esconde o ProgressBar em caso de erro
                // Handle onCancelled
            }
        })
    }
    private fun hideRatingElements() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_proposals)?.visibility = View.GONE
    }
    private fun showRatingElements() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_proposals)?.visibility = View.VISIBLE
    }
    private fun hidePortfolioRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_proposals_receive)?.visibility = View.GONE
    }
    private fun showPortfolioRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_proposals_receive)?.visibility = View.VISIBLE
    }

    private fun acceptProposal(proposal: Proposals) {

        // Verifica se a proposta já foi aceita ou rejeitada
        if (proposal.accepted == "Aprovado" || proposal.rejected == "Reprovado") {
            // Mostrar mensagem informando que a proposta já foi processada
            // Você pode adicionar um Toast ou Snackbar para informar o usuário
            Toast.makeText(requireContext(), "Esta proposta já foi aprovada", Toast.LENGTH_SHORT).show()
            return
        }

        val proposalId = proposal.proposalId ?: return
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("Proposals").child(proposalId)

        // Mostrar um diálogo de confirmação
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmar Aceitação")
        builder.setMessage("Você tem certeza que deseja aceitar esta proposta?")

        builder.setPositiveButton("Sim") { dialog, which ->
            // Atualizar o status da proposta para "Aprovado"
            databaseReference.child("accepted").setValue("Aprovado")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Atualiza o RecyclerView após aceitar a proposta
                        proposalsSingleItemAdapter?.updateData(proposalList ?: listOf())
                        Log.d("ProposalsFragment", "Proposta aceita com sucesso")



                        // se o postAdapter inicializado
                      //  updateProposalCountInStatistic(true)

                        // Atualize a UI ou realize outras ações após aceitar a proposta, se necessário
                        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date())

                        proposal.prazoAceitacao = currentDate // Atualiza prazoAceitacao da proposta
                        createManageService(proposal)
                        createManageProject(proposal)
                        createManageServiceHistory(proposal)
                        acceptProposalStatus(proposal.userId)
                        sharedViewModel.acceptedProposal.value = proposal

                        updateProposalCountInStatistic(true)
                        updatePropoCountInStatistic(true)

                        loadUserData(firebaseUser?.uid ?: "") { userName, userProfileImage ->
                            // Enviar uma notificação para o usuário que fez a proposta
                            addNotification(proposal.userId ?: "", proposal.postId ?: "", userName, userProfileImage, proposal.projectTitle ?: "")

                            // Atualize os detalhes do usuário que fez a proposta com os detalhes do usuário que aceitou a proposta
                            updateProposerUserDetails(proposal.userId ?: "", userName, userProfileImage)
                        }

                        // Enviar uma mensagem para todos os usuários que fizeram propostas no post
                        sendConfirmationMessageToUsers(proposal)
                    } else {
                        Log.e("ProposalsFragment", "Erro ao aceitar a proposta", task.exception)
                        // Trate o erro conforme necessário
                    }
                }
        }

        builder.setNegativeButton("Não") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
    private fun acceptProposalStatus(userId: String?) {
        val sharedPref = requireContext().getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("proposalAccepted_$userId", true)
            apply()
        }
    }
    private fun rejectedProposal(proposal: Proposals) {
        // Verifica se a proposta já foi aceita ou rejeitada
        if (proposal.accepted == "Aprovado" || proposal.rejected == "Reprovado") {
            // Mostrar mensagem informando que a proposta já foi processada
            // Você pode adicionar um Toast ou Snackbar para informar o usuário
            Toast.makeText(requireContext(), "Esta proposta já foi reprovado ou aprovado", Toast.LENGTH_SHORT).show()
            return
        }

        val proposalId = proposal.proposalId ?: return
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("Proposals").child(proposalId)

        // Mostrar um diálogo de confirmação
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmar Rejeição")
        builder.setMessage("Você tem certeza que deseja rejeitar esta proposta?")

        builder.setPositiveButton("Sim") { dialog, which ->
            // Removendo listeners para evitar chamadas múltiplas
            // Para garantir que apenas uma ação seja realizada por proposta
            // Você pode adicionar os listeners novamente após o processamento, se necessário
            //val holder = holder // Se você tiver acesso ao holder aqui, use-o para remover listeners

            // Atualizar o status da proposta para "Reprovado"
            databaseReference.child("rejected").setValue("Reprovado")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Atualiza o RecyclerView após rejeitar a proposta
                        proposalsSingleItemAdapter?.updateData(proposalList ?: listOf())
                        Log.d("ProposalsFragment", "Proposta rejeitada com sucesso")
                        updateProposalCountInStatistic(false)
                        updatePropoCountInStatistic(false)

                        // Enviar notificação de proposta rejeitada
                        loadUserData(firebaseUser?.uid ?: "") { userName, userProfileImage ->
                            // Enviar uma notificação para o usuário que fez a proposta
                            addNotificationReject(proposal.userId ?: "", proposal.postId ?: "", userName, userProfileImage, proposal.projectTitle ?: "")

                            // Atualize os detalhes do usuário que fez a proposta com os detalhes do usuário que aceitou a proposta
                            updateProposerUserDetails(proposal.userId ?: "", userName, userProfileImage)
                        }

                        // Atualize a UI ou realize outras ações após rejeitar a proposta, se necessário
                        proposalList?.remove(proposal) // Remove a proposta da lista após rejeição
                        proposalsAdapter?.notifyDataSetChanged() // Notifica o adapter sobre a mudança nos dados
                    } else {
                        Log.e("ProposalsFragment", "Erro ao rejeitar a proposta", task.exception)
                        // Trate o erro conforme necessário
                    }
                }
        }

        builder.setNegativeButton("Não") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
    private fun sendConfirmationMessageToUsers(proposal: Proposals) {
        val postId = proposal.postId ?: return

        // Buscar todas as propostas relacionadas a este post
        val proposalsRef = FirebaseDatabase.getInstance().reference.child("Proposals")

        proposalsRef.orderByChild("postId").equalTo(postId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (proposalSnapshot in snapshot.children) {
                    val proposal = proposalSnapshot.getValue(Proposals::class.java)
                    proposal?.let {
                        val userId = it.userIdOther ?: return
                        sendMessageToUser(userId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
    private fun sendMessageToUser(userId: String) {
        val message = "Sua proposta foi aceita!"
        val messageId = FirebaseDatabase.getInstance().reference.child("Messages").push().key ?: return

        val messageMap = HashMap<String, Any>()
        messageMap["messageId"] = messageId
        messageMap["userId"] = userId
        messageMap["message"] = message

        FirebaseDatabase.getInstance().reference.child("Messages").child(messageId).setValue(messageMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sucesso ao enviar a mensagem
                } else {
                    // Falha ao enviar a mensagem
                }
            }
    }
    private fun createManageService(proposal: Proposals) {
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
                        createdAt = currentTime,
                        expirationDate = proposal.numberDays
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
    private fun createManageProject(proposal: Proposals) {
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
    private fun createManageServiceHistory(proposal: Proposals) {
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
                            userIdOther = proposal?.userIdOther?: "",
                            postId = proposal.postId ?: "",
                            userId = proposal.userId!!,
                            status = "",
                            money = manageService?.money?: "",
                            projectDate = manageService?.projectDate?: "",
                            workerName = manageService?.workerName ?: "",
                            clientName = manageService?.clientName ?: "", // Usando workerName do ManageService
                            projectName = proposal.projectTitle ?: "", // Usando clientName do ManageService
                            expirationDate = manageService?.expirationDate ?: "",
                            createdAt = manageService?.createdAt ?: "", // Defina o prazo conforme necessário

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


    private fun updatePropoCountInStatistic(isAccepted: Boolean) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ServiceCount")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val proposalsStatistic = dataSnapshot.getValue(ServiceCount::class.java)
                    proposalsStatistic?.let {

                        if (isAccepted) {
                            // Incrementa o contador de propostas aceitas
                            it.proposalsAcceptCount += 1
                        } else {
                            // Incrementa o contador de propostas recusadas
                            it.proposalsRefuseCount += 1
                        }
                        // Atualiza o valor na base de dados
                        databaseReference.setValue(it)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Sucesso ao atualizar o contador de propostas
                                } else {
                                    // Falha ao atualizar o contador de propostas
                                }
                            }
                    }
                } else {
                    // Se a entrada não existir, você pode criar uma nova entrada aqui
                    val newStatistic = ServiceCount(
                        postsCount = 0,
                        propCount = 0,
                        proposalsAcceptCount = if (isAccepted) 1 else 0,
                        proposalsRefuseCount = if (isAccepted) 0 else 1,
                        concludeCount = 0,
                        cancelCount = 0,

                    )
                    // Cria uma nova entrada na base de dados
                    databaseReference.setValue(newStatistic)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Sucesso ao criar uma nova entrada
                            } else {
                                // Falha ao criar uma nova entrada
                            }
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
    private fun updateProposalCountInStatistic(isAccepted: Boolean) {
        val userId = firebaseUser?.uid ?: return // Verifica se o usuário está autenticado
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ProposalStats").child(userId)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val proposalsStatistic = dataSnapshot.getValue(ProposalsStatistic::class.java)
                    proposalsStatistic?.let {
                        // Incrementa o contador de propostas recebidas
                        it.proposalsCount += 1
                        if (isAccepted) {
                            // Incrementa o contador de propostas aceitas
                            it.proposalsAcceptCount += 1
                        } else {
                            // Incrementa o contador de propostas recusadas
                            it.proposalsRefuseCount += 1
                        }
                        // Atualiza o valor na base de dados
                        databaseReference.setValue(it)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Sucesso ao atualizar o contador de propostas
                                } else {
                                    // Falha ao atualizar o contador de propostas
                                }
                            }
                    }
                } else {
                    // Se a entrada não existir, você pode criar uma nova entrada aqui
                    val newStatistic = ProposalsStatistic(
                        userId = userId,
                        proposalsCount = 1,
                        proposalsAcceptCount = if (isAccepted) 1 else 0,
                        proposalsRefuseCount = if (isAccepted) 0 else 1
                    )
                    // Cria uma nova entrada na base de dados
                    databaseReference.setValue(newStatistic)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Sucesso ao criar uma nova entrada
                            } else {
                                // Falha ao criar uma nova entrada
                            }
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
    private fun checkInternetConnection(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }
    private fun updateFragmentOnInternetAvailable() {
        if (checkInternetConnection()) {
            // Recarregar os dados e atualizar o fragmento
            retrieveProposals()
        }
    }
    override fun onResume() {
        super.onResume()
        // Verificar a conexão de rede e atualizar o fragmento se necessário
        updateFragmentOnInternetAvailable()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        activity?.registerReceiver(networkChangeReceiver, intentFilter)
    }
    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(networkChangeReceiver)
    }
    private fun scheduleDataRetrieval() {
        handler.postDelayed({
            retrieveProposals()
        }, 3000) // 3000ms = 3 segundos
    }
    private fun addNotification(userId: String, postId: String, userName: String, userProfileImage: String?, projectTitle: String) {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(userId)
        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = firebaseUser!!.uid
        notiMap["postTitle"] = "Aceitou a tua proposta para o serviço:$projectTitle"
        notiMap["postId"] = postId
        notiMap["ispost"] = true
        notiMap["userName"] = userName
        notiMap["userProfileImage"] = userProfileImage ?: ""

        notiRef.push().setValue(notiMap)
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
    private fun addNotificationReject(userId: String, postId: String, userName: String, userProfileImage: String?, projectTitle: String) {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(userId)
        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = firebaseUser!!.uid
        notiMap["postTitle"] = "Recusou a tua proposta para o serviço:$projectTitle"
        notiMap["postId"] = postId
        notiMap["ispost"] = true
        notiMap["userName"] = userName
        notiMap["userProfileImage"] = userProfileImage ?: ""

        notiRef.push().setValue(notiMap)
    }
}