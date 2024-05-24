package com.example.rassoonlineapp.View

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.ManageContractClientAdapter
import com.example.rassoonlineapp.Model.ManageContract
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageContractClientActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ManageContractClientAdapter
    private lateinit var manageProjectList: MutableList<ManageContract>

    private var manageContractId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_contract_client)

        manageContractId = intent.getStringExtra("manageContractId") ?: ""
        recyclerView = findViewById(R.id.recycler_view_manage_client_contract)
        recyclerView.layoutManager = LinearLayoutManager(this)
        manageProjectList = mutableListOf()
        adapter = ManageContractClientAdapter(this, manageProjectList)
        recyclerView.adapter = adapter

        retrieveManageProjectFromFirebase(manageContractId)
    }

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
                    Log.e("ManageContractClientActivity", "No data found for manageContractId: $manageContractId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ManageContractClientActivity", "Database error: ${databaseError.message}")
            }
        })
    }
}
