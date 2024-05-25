package com.example.rassoonlineapp.View

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.ManageContractWorkerAdapter
import com.example.rassoonlineapp.Model.ManageContract
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageContractWorkerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ManageContractWorkerAdapter
    private lateinit var manageProjectList: MutableList<ManageContract>

    private var manageContractId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_contract_worker)

        manageContractId = intent.getStringExtra("manageContractId") ?: ""
        recyclerView = findViewById(R.id.recycler_view_contract_worker)
        recyclerView.layoutManager = LinearLayoutManager(this)
        manageProjectList = mutableListOf()
        adapter = ManageContractWorkerAdapter(this, manageProjectList)
        recyclerView.adapter = adapter

        retrieveManageProjectFromFirebase(manageContractId)
    }
/*
    private fun retrieveManageProjectFromFirebase(manageContractId: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ManageContracts").child(manageContractId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Verifique se há dados
                if (dataSnapshot.exists()) {
                    // Obtenha o objeto ManageContract do primeiro filho
                    val manageProject = dataSnapshot.children.firstOrNull()?.getValue(ManageContract::class.java)
                    manageProject?.let {
                        manageProjectList.add(it)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    // Não há dados para o ID fornecido
                 //   Log.e("ManageContractWorkerActivity", "No data found for manageContractId: $manageContractId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
             //   Log.e("ManageContractWorkerActivity", "Database error: ${databaseError.message}")
            }
        })
    }

 */

    private fun retrieveManageProjectFromFirebase(manageContractId: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ManageContracts").child(manageContractId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val manageProject = dataSnapshot.getValue(ManageContract::class.java)
                    manageProject?.let {
                        manageProjectList.add(it)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    Log.e("ManageContractWorkerActivity", "No data found for manageContractId: $manageContractId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ManageContractWorkerActivity", "Database error: ${databaseError.message}")
            }
        })
    }


}
