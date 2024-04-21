package com.example.rassoonlineapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.History
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class HistoryPostAdapter(private val historyList: List<History>) :
    RecyclerView.Adapter<HistoryPostAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userProfileImage = itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.user_profile_image_post)
        val userName = itemView.findViewById<TextView>(R.id.user_name_post)
        val dataHora = itemView.findViewById<TextView>(R.id.data_hora)
        val textViewTittle = itemView.findViewById<TextView>(R.id.textView_tittle)
        val textViewDescription = itemView.findViewById<TextView>(R.id.textView_description)
        val textViewSkills = itemView.findViewById<TextView>(R.id.textView_skills)
        val textViewWork = itemView.findViewById<TextView>(R.id.textView_work)
        val textViewPreco = itemView.findViewById<TextView>(R.id.textView_preco)
        val textViewPrazo = itemView.findViewById<TextView>(R.id.textView_prazo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_history_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = historyList[position]

        // Aqui você pode definir os dados para as views do item
        holder.userName.text = history.userName
        holder.dataHora.text = history.data_hora
        holder.textViewTittle.text = history.titulo
        holder.textViewDescription.text = history.descricao
        holder.textViewSkills.text = history.habilidades?.joinToString(", ") ?: ""
        holder.textViewWork.text = history.tipoTrabalho
        holder.textViewPreco.text = history.orcamento
        holder.textViewPrazo.text = history.prazo
        // Aqui você pode definir o perfil da imagem do usuário, se necessário
         //holder.userProfileImage.setImageURI(history.userProfileImage)

        loadUserData(history.userId.toString(), history, holder)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }


    private fun loadUserData(userId: String, history: History, holder: HistoryPostAdapter.ViewHolder) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    // Adiciona o nome de usuário e a imagem do usuário ao objeto Post
                    history.userName = user?.getUsername()
                    history.userProfileImage = user?.getImage()

                    // Verifica se o URL da imagem não é nulo ou vazio antes de carregá-lo
                    val userProfileImage = user?.getImage()
                    if (!userProfileImage.isNullOrBlank()) {
                        Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).
                        into(holder.userProfileImage)
                    }

                    holder.userName.text = user?.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}
