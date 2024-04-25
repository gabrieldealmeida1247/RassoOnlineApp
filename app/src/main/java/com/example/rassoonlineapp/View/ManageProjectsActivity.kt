package com.example.rassoonlineapp.View

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rassoonlineapp.Adapter.ManageProjectAdapter
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.ViewModel.WorkManager.ManageWorker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class ManageProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ManageProjectAdapter
    private lateinit var manageProjectList: MutableList<ManageProject>
    // Dentro da classe ManageProjectsActivity
    private lateinit var coroutineScope: CoroutineScope
    private val job = Job()


    // Variável para armazenar o ID do projeto que você deseja visualizar
    private var manageId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_projects)


        // Inicialize o CoroutineScope
        coroutineScope = CoroutineScope(Dispatchers.Main + job)

        // Inicie o WorkManager
        startManageWork()

        // Aqui você precisa obter o manageId do intent ou de onde quer que venha
        manageId = intent.getStringExtra("manageId") ?: ""

        // Aqui você precisa chamar o método para recuperar os dados do Firebase e atualizar o adaptador
        retrieveManageProjectFromFirebase(manageId)

    }

    private fun startManageWork() {
        val workRequest = OneTimeWorkRequestBuilder<ManageWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancela todas as coroutines quando a activity é destruída
    }
/*
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


 */



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
                    manageProject.isCompleted = manageProject.status == "Concluído"
                    manageProject.isCancelled = manageProject.status == "Cancelado"
                    manageProjectList.add(manageProject)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }




}