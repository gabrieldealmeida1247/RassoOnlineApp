package com.example.rassoonlineapp.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ViewSwitcher
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.ProposalAdapter
import com.example.rassoonlineapp.Adapter.ProposalsSingleItemAdapter
import com.example.rassoonlineapp.Model.ManageService
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProposalsFragment : Fragment() {
    private var proposalsAdapter: ProposalAdapter? = null
    private var proposalList: MutableList<Proposals>? = null
    private var firebaseUser: FirebaseUser? = null
    private var proposalsRef: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_proposals, container, false)
        val viewSwitcher = view.findViewById<ViewSwitcher>(R.id.view_switcher_proposols)

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
            val recyclerViewProposolsReceive =
                view.findViewById<RecyclerView>(R.id.recycler_view_proposals_receive)
            recyclerViewProposolsReceive.layoutManager =
                LinearLayoutManager(context) // Adicione um gerenciador de layout se necessário

            // Filtra a lista de propostas para exibir apenas as propostas de outros usuários
            val otherUserProposals = proposalList?.filter { it.userId != firebaseUser?.uid }
            val proposalsAdapter = ProposalsSingleItemAdapter(otherUserProposals ?: listOf())

            proposalsAdapter.setAcceptListener(object :
                ProposalsSingleItemAdapter.ProposalAcceptListener {
                override fun onProposalAccepted(proposal: Proposals) {
                    acceptProposal(proposal)
                    createManageService(proposal)
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
            }

            override fun onCancelled(error: DatabaseError) {
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
        val proposalId = proposal.proposalId ?: return
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("Proposals").child(proposalId)
        databaseReference.child("accepted").setValue("Aprovado")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProposalsFragment", "Proposta aceita com sucesso")
                    // Atualize a UI ou realize outras ações após aceitar a proposta, se necessário
                    createManageService(proposal)
                } else {
                    Log.e("ProposalsFragment", "Erro ao aceitar a proposta", task.exception)
                    // Trate o erro conforme necessário
                }
            }
    }

    private fun rejectedProposal(proposal: Proposals) {
        val proposalId = proposal.proposalId ?: return
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("Proposals").child(proposalId)
        databaseReference.child("rejected").setValue("Reprovado")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProposalsFragment", "Proposta reprovada com sucesso")
                    // Atualize a UI ou realize outras ações após aceitar a proposta, se necessário
                    // Atualiza a lista de propostas no adapter
                    proposalList?.remove(proposal) // Remove a proposta aceita da lista
                    proposalsAdapter?.notifyDataSetChanged() // Notifica o adapter sobre a mudança nos dados
                } else {
                    Log.e("ProposalsFragment", "Erro ao reprovar a proposta", task.exception)
                    // Trate o erro conforme necessário
                }
            }
    }

    private fun createManageService(proposal: Proposals) {
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("ManageService").child(proposal.proposalId!!)

        val manageServiceId = proposal.proposalId // Usando proposalId como a chave
        val manageService = ManageService(
            serviceId = manageServiceId!!,
            proposalId = proposal.proposalId!!,
            userId = proposal.userId!!,
            status = "ativo",
            money = proposal.lance!!,
            projectDate = proposal.numberDays!!,
            workerName = "Andrade Developer",
            clientName = "Fernando",
            projectName = proposal.projectTitle!!,
            expirationDate = "22 de Maio"
        )

        databaseReference.setValue(manageService) // Definindo o valor diretamente com setValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sucesso ao criar o ManageService
                } else {
                    // Falha ao criar o ManageService
                }
            }
    }



}
