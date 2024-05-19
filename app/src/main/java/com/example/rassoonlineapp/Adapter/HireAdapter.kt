package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Hire
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HireAdapter(
    private val mContext: Context,
    private val mHire: List<Hire>,
) : RecyclerView.Adapter<HireAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.hire_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mHire.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val hire = mHire[position]

        // Carrega as informações do usuário
        loadUserData(hire.userId, holder)

        holder.tittle.text = hire.projectName
        holder.description.text = hire.comments
        holder.orcamento.text = hire.editPrice.toString()

        // Formatar e exibir a data/hora
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(hire.timestamp)
        holder.dateHour.text = sdf.format(date)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_hire)
        var userName: TextView = itemView.findViewById(R.id.user_name_hire)
        var tittle: TextView = itemView.findViewById(R.id.textView_tittle)
        var description: TextView = itemView.findViewById(R.id.textView_description)
        var orcamento: TextView = itemView.findViewById(R.id.textView_preco)
        var deletePost: ImageView = itemView.findViewById(R.id.delete_post)
        var dateHour: TextView = itemView.findViewById(R.id.data_hora)
    }

    private fun loadUserData(userId: String, holder: ViewHolder) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        holder.userName.text = it.getUsername()
                        if (!it.getImage().isNullOrEmpty()) {
                            Picasso.get().load(it.getImage()).placeholder(R.drawable.profile).into(holder.profileImage)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}
