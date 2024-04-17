package com.example.rassoonlineapp
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.ManageServiceAdapter
import com.example.rassoonlineapp.Adapter.ManageServiceWorkerAdapter
import com.example.rassoonlineapp.Model.ManageService
import com.example.rassoonlineapp.Model.Proposals
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ServiceManageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewWork: RecyclerView
    private lateinit var adapter: ManageServiceAdapter
    private lateinit var workerAdapter: ManageServiceWorkerAdapter
    private lateinit var manageServicesList: MutableList<ManageService>
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference
    private lateinit var proposalsList: MutableList<Proposals>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_manage)
        val viewSwitcher = findViewById<ViewSwitcher>(R.id.view_switcher_manage)

        // Inicializar o firebaseUser
        firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        
        findViewById<Button>(R.id.button_manage_client).setOnClickListener {
            viewSwitcher.setDisplayedChild(0)
            hideWorkerRecyclerView()
            showClientRecyclerView()

        }


        findViewById<Button>(R.id.button_manage_worker).setOnClickListener {
            viewSwitcher.setDisplayedChild(1)
            hideClientRecyclerView()
            showWorkerRecyclerView()

          serviceWorkList()
        }

        serviceClientList()

    }

    private fun hideClientRecyclerView() {
        findViewById<RecyclerView>(R.id.recycler_view)?.visibility = View.GONE
    }

    private fun showClientRecyclerView() {
        // Exibe a RecyclerView de portfólio
        findViewById<RecyclerView>(R.id.recycler_view)?.visibility = View.VISIBLE
    }

    private fun hideWorkerRecyclerView() {
        findViewById<RecyclerView>(R.id.recycler_view_manage_worker)?.visibility = View.GONE
    }

    private fun showWorkerRecyclerView() {
        findViewById<RecyclerView>(R.id.recycler_view_manage_worker)?.visibility = View.VISIBLE
    }

/*
    private fun serviceClientList() {
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        manageServicesList = mutableListOf()
        adapter = ManageServiceAdapter(this, manageServicesList)
        recyclerView.adapter = adapter


        // Obtendo uma referência para o nó "ManageService" no banco de dados
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ManageService")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                manageServicesList.clear()
                for (manageServiceSnapshot in dataSnapshot.children) {
                    // Obtendo os dados do post
                    val manageService = manageServiceSnapshot.getValue(ManageService::class.java)
                    manageService?.let {
                        // Obtendo o userId do dono do post
                        val postOwnerId = it.userId
                        // Adicionando o post à lista apenas se o userId do dono do post for diferente do userId atual
                        if (postOwnerId != firebaseUser.uid) {
                            manageServicesList.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Tratar erros de leitura do banco de dados
            }
        })
    }

 */
private fun serviceClientList() {
    recyclerView = findViewById(R.id.recycler_view)
    recyclerView.layoutManager = LinearLayoutManager(this)
    manageServicesList = mutableListOf()
    proposalsList = mutableListOf() // Lista para as propostas
    adapter = ManageServiceAdapter(this, manageServicesList)
    recyclerView.adapter = adapter

    // Obtendo uma referência para o nó "ManageService" e "Proposals" no banco de dados
    val databaseReference = FirebaseDatabase.getInstance().reference

    // ValueEventListener para "Proposals" para obter userIdOther
    databaseReference.child("Proposals").addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            for (proposalSnapshot in dataSnapshot.children) {
                val proposal = proposalSnapshot.getValue(Proposals::class.java)
                proposal?.let {
                    val userIdOther = it.userIdOther // Obtendo userIdOther da proposta
                    // ValueEventListener para "ManageService"
                    databaseReference.child("ManageService").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            manageServicesList.clear()
                            for (manageServiceSnapshot in dataSnapshot.children) {
                                val manageService = manageServiceSnapshot.getValue(ManageService::class.java)
                                manageService?.let {
                                    if (it.userId != firebaseUser.uid && it.userId != userIdOther && userIdOther == firebaseUser.uid) {
                                        manageServicesList.add(it)
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Tratar erros de leitura do banco de dados
                        }
                    })
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Tratar erros de leitura do banco de dados
        }
    })

    // ValueEventListener para "proposals"
    databaseReference.child("Proposals").addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            proposalsList.clear() // Limpar a lista de propostas
            for (proposalSnapshot in dataSnapshot.children) {
                val proposal = proposalSnapshot.getValue(Proposals::class.java)
                proposal?.let {
                    proposalsList.add(it) // Adicionar a proposta à lista de propostas
                    if (it.userId != firebaseUser.uid && it.userId == it.userIdOther) {
                        // Faça algo com a proposta que atende a condição
                    }
                }
            }
            adapter.notifyDataSetChanged() // Notificar o adaptador sobre as mudanças
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Tratar erros de leitura do banco de dados
        }
    })
}




    private fun serviceWorkList(){
        recyclerViewWork = findViewById(R.id.recycler_view_manage_worker)
        recyclerViewWork.layoutManager = LinearLayoutManager(this)
        manageServicesList = mutableListOf()
        workerAdapter = ManageServiceWorkerAdapter(this, manageServicesList)
        recyclerViewWork.adapter = workerAdapter

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        // Acessando diretamente o nó "ManageService" no banco de dados e filtrando por userId
        databaseReference = FirebaseDatabase.getInstance().reference.child("ManageService")
        val query = databaseReference.orderByChild("userId").equalTo(firebaseUser.uid)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                manageServicesList.clear()
                for (manageServiceSnapshot in dataSnapshot.children) {
                    val manageService = manageServiceSnapshot.getValue(ManageService::class.java)
                    manageService?.let {
                        manageServicesList.add(it)
                    }
                }
                workerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }
}

