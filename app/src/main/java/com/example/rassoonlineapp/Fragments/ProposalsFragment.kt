package com.example.rassoonlineapp.Fragments

import android.os.Bundle
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
            val recyclerViewProposolsReceive = view.findViewById<RecyclerView>(R.id.recycler_view_proposals_receive)
            recyclerViewProposolsReceive.layoutManager = LinearLayoutManager(context) // Adicione um gerenciador de layout se necessário
            recyclerViewProposolsReceive.adapter = ProposalsSingleItemAdapter(proposalList!!) // Aqui você define o adaptador
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        proposalsRef = FirebaseDatabase.getInstance().reference.child("Proposals")

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_proposals)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        proposalList = ArrayList()
        proposalsAdapter = ProposalAdapter(proposalList as ArrayList<Proposals>) // Corrigido para usar ProposalAdapter
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


}
