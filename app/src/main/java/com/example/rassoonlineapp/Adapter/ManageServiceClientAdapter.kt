package com.example.rassoonlineapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageServiceClientAdapter(private val serviceList: List<Proposals>) : RecyclerView.Adapter<ManageServiceClientAdapter.ViewHolder>() {

    private val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private val usersReference = FirebaseDatabase.getInstance().reference.child("Users")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.manage_client_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = serviceList[position]
        loadProposalData(service.userId.toString(), service, holder)
        holder.dinheiro.text = service.lance
        holder.prazo.text = service.numberDays
        holder.title.text = service.projectTitle
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val textViewNameClient: TextView = itemView.findViewById(R.id.textView_name_client)
        val days: TextView = itemView.findViewById(R.id.days)
        val status: TextView = itemView.findViewById(R.id.status)
        val prazo: TextView = itemView.findViewById(R.id.prazo)
        val dinheiro: TextView = itemView.findViewById(R.id.dinheiro)
    }

    private fun loadProposalData(userId: String, proposals: Proposals, holder: ViewHolder) {
        val usersRef = usersReference.child(userId)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    proposals.username = user?.getUsername()
                    holder.textViewNameClient.text = user?.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        }
        usersRef.addListenerForSingleValueEvent(valueEventListener)
    }
}

