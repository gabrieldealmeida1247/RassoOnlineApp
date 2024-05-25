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
import com.example.rassoonlineapp.Adapter.ProposalsStatisticChartAdapter
import com.example.rassoonlineapp.Adapter.ServiceStatisticChartAdapter
import com.example.rassoonlineapp.Model.ProposalsStatistic
import com.example.rassoonlineapp.Model.Statistic
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GraphicFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServiceStatisticChartAdapter
    private  var chartList: MutableList<Statistic> = mutableListOf()
    private lateinit var proposalsStatisticChartAdapter: ProposalsStatisticChartAdapter
    private  var proposalsList: MutableList<ProposalsStatistic> = mutableListOf()
    private  var chartRef: DatabaseReference? = null

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_graphic, container, false)

        val viewSwitcher = view.findViewById<ViewSwitcher>(R.id.view_switcher_history)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        viewSwitcher.post { viewSwitcher.setDisplayedChild(0) }


        view.findViewById<Button>(R.id.button_estatistic_service).setOnClickListener {
            // Lógica para exibir o layout principal no ViewSwitcher
            viewSwitcher.setDisplayedChild(0)
            hideProposalsStatistic()
            showServiceStatistic()

        }


        view.findViewById<Button>(R.id.button_estatistic_proposals).setOnClickListener {
            // Lógica para exibir o layout principal no ViewSwitcher
            viewSwitcher.setDisplayedChild(1)
            hideServiceStatistic()
            showProposalsStatistic()

            proposalsStatisticChartAdapter = ProposalsStatisticChartAdapter(requireContext(), proposalsList)
            recyclerView = view.findViewById(R.id.recycler_view_proposals_estatistic)
            recyclerView.adapter = proposalsStatisticChartAdapter
            recyclerView.layoutManager = LinearLayoutManager(context)


            fetchProposalsStatisticsFromFirebase()

        }


        view.findViewById<Button>(R.id.button_contract).setOnClickListener {
            // Lógica para exibir o layout principal no ViewSwitcher
            viewSwitcher.setDisplayedChild(2)
            hideProposalsStatistic()
            hideServiceStatistic()
            hidePayment()
            showContract()

        }

        view.findViewById<Button>(R.id.button_payment).setOnClickListener {
            // Lógica para exibir o layout principal no ViewSwitcher
            viewSwitcher.setDisplayedChild(3)
            hideProposalsStatistic()
            hideServiceStatistic()
            hideContract()
            showPayment()

        }

        chartRef = FirebaseDatabase.getInstance().reference.child("Statistic")
        adapter = ServiceStatisticChartAdapter(requireContext(),chartList)
        recyclerView = view.findViewById(R.id.recycler_view_service_estatistic)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
            // Recuperar os dados do Firebase e atualizar o adaptador
        fetchStatisticsFromFirebase()

        return view
    }

    private fun fetchStatisticsFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("Statistics")

        val statisticsList = mutableListOf<Statistic>()

        val currentUserId = firebaseUser?.uid

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val statistic = postSnapshot.getValue(Statistic::class.java)
                    statistic?.let {
                        if (it.userId == currentUserId) {
                            statisticsList.add(it)
                        }
                    }
                }

                // Atualizar o adaptador com os dados recuperados
                adapter = ServiceStatisticChartAdapter(requireContext(),statisticsList)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }


    private fun fetchProposalsStatisticsFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ProposalStats")

        val currentUserId = firebaseUser?.uid

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                proposalsList.clear()
                for (postSnapshot in snapshot.children) {
                    val proposalStatistic = postSnapshot.getValue(ProposalsStatistic::class.java)
                    proposalStatistic?.let {
                        if (it.userId == currentUserId) {
                            proposalsList.add(it)
                        }
                    }
                }

                // Notificar o adapter que os dados mudaram
                proposalsStatisticChartAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun hideServiceStatistic() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_service_estatistic)?.visibility = View.GONE
    }
    private fun showServiceStatistic() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_service_estatistic)?.visibility = View.VISIBLE
    }


    private fun hideProposalsStatistic() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_proposals_estatistic)?.visibility = View.GONE
    }
    private fun showProposalsStatistic() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_proposals_estatistic)?.visibility = View.VISIBLE
    }


    private fun hidePayment() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_payment)?.visibility = View.GONE
    }
    private fun showPayment() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_payment)?.visibility = View.VISIBLE
    }

    private fun hideContract() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_contract)?.visibility = View.GONE
    }
    private fun showContract() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_contract)?.visibility = View.VISIBLE
    }
}
