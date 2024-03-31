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
import com.example.rassoonlineapp.Adapter.ManageServiceClientAdapter
import com.example.rassoonlineapp.Adapter.ManageServiceWorkerAdapter
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageServiceFragment : Fragment() {
    private var manageServiceClientAdapter: ManageServiceClientAdapter? = null
    private var serviceList: MutableList<Proposals>? = null
    private var firebaseUser: FirebaseUser? = null
    private var serviceRef: DatabaseReference? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         val view = inflater.inflate(R.layout.fragment_manage_service, container, false)

        val viewSwitcher = view.findViewById<ViewSwitcher>(R.id.view_switcher_manage)

        view.findViewById<Button>(R.id.button_manage_client).setOnClickListener {
            // Lógica para exibir o layout principal no ViewSwitcher
            viewSwitcher.setDisplayedChild(0)
            hidePortfolioRecyclerView()
            showRatingElements()

            }

        view.findViewById<Button>(R.id.button_manage_worker).setOnClickListener {
            // Lógica para exibir o layout de portfólio no ViewSwitcher
            viewSwitcher.setDisplayedChild(1)
            showPortfolioRecyclerView()
            hideRatingElements()

            // Inflar o layout do item de portfólio diretamente na RecyclerView
            val recyclerViewServiceWorker = view.findViewById<RecyclerView>(R.id.recycler_view_manage_worker)
            recyclerViewServiceWorker.layoutManager = LinearLayoutManager(context) // Adicione um gerenciador de layout se necessário

            // Filtra a lista de propostas para exibir apenas as propostas de outros usuários
            val otherUserService = serviceList?.filter { it.userId != firebaseUser?.uid }
            recyclerViewServiceWorker.adapter = ManageServiceWorkerAdapter(otherUserService ?: listOf())
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        serviceRef = FirebaseDatabase.getInstance().reference.child("Proposals")

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_manage_client)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        serviceList = ArrayList()
        manageServiceClientAdapter = ManageServiceClientAdapter(serviceList as ArrayList<Proposals>) // Corrigido para usar ProposalAdapter
        recyclerView.adapter = manageServiceClientAdapter

        retrieveServiceClient()


        return view
    }


    private fun retrieveServiceClient() {
        serviceRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serviceList?.clear()

                for (proposalSnapshot in snapshot.children) {
                    val proposal = proposalSnapshot.getValue(Proposals::class.java)
                    proposal?.let {
                        serviceList?.add(it)
                    }
                }

                manageServiceClientAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


    private fun hideRatingElements() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_manage_client)?.visibility = View.GONE
    }

    private fun showRatingElements() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_manage_client)?.visibility = View.VISIBLE
    }

    private fun hidePortfolioRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_manage_worker)?.visibility = View.GONE
    }

    private fun showPortfolioRecyclerView() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_manage_worker)?.visibility = View.VISIBLE
    }


}