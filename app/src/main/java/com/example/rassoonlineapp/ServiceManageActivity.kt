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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_manage)
        val viewSwitcher = findViewById<ViewSwitcher>(R.id.view_switcher_manage)


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


    private fun serviceClientList(){
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        manageServicesList = mutableListOf()
        adapter = ManageServiceAdapter(this, manageServicesList)
        recyclerView.adapter = adapter

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
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
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
