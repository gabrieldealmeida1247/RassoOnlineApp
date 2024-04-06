package com.example.rassoonlineapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.ManageProjectAdapter
import com.example.rassoonlineapp.Model.ManageProject
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ManageProjectAdapter
    private lateinit var manageProjectList: MutableList<ManageProject>
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference

    // Variável para armazenar o ID do projeto que você deseja visualizar
    private var manageId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_projects)

        // Aqui você precisa obter o manageId do intent ou de onde quer que venha
       manageId = intent.getStringExtra("manageId") ?: ""

        // Aqui você precisa chamar o método para recuperar os dados do Firebase e atualizar o adaptador
        retrieveManageProjectFromFirebase(manageId)

    }

    private fun retrieveManageProjectFromFirebase(manageId: String) {
        recyclerView = findViewById(R.id.recycler_view_manage_projects)
        recyclerView.layoutManager = LinearLayoutManager(this)
        manageProjectList = mutableListOf()
        adapter = ManageProjectAdapter(this, manageProjectList)
        recyclerView.adapter = adapter

        val databaseReference = FirebaseDatabase.getInstance().reference.child("ManageProject")
        databaseReference.child(manageId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val manageProject = dataSnapshot.getValue(ManageProject::class.java)
                manageProject?.let {
                    manageProjectList.add(it)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Trate o erro aqui, se necessário
            }
        })
    }

}