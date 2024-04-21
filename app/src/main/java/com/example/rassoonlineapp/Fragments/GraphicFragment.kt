package com.example.rassoonlineapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.ServiceStatisticChartAdapter
import com.example.rassoonlineapp.Model.Statistic
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GraphicFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServiceStatisticChartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_graphic, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_service_estatistic)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Recuperar os dados do Firebase e atualizar o adaptador
        fetchStatisticsFromFirebase()

        return view
    }

    private fun fetchStatisticsFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("statistics")

        val statisticsList = mutableListOf<Statistic>()

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val statistic = postSnapshot.getValue(Statistic::class.java)
                    statistic?.let {
                        statisticsList.add(it)
                    }
                }

                // Atualizar o adaptador com os dados recuperados
                adapter = ServiceStatisticChartAdapter(statisticsList)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }
}
