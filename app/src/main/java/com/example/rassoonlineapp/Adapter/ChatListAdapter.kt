package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ChatList
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

class ChatListAdapter(private val mContext: Context, private val ChatList: List<ChatList>) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

        private val MESSAGE_TYPE_LEFT = 0
        private val MESSAGE_TYPE_RIGHT = 1

        var firebaseUser: FirebaseUser? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == MESSAGE_TYPE_RIGHT) {
            val view = LayoutInflater.from(mContext).inflate(R.layout.item_right, parent, false)
            return ViewHolder(view)
        }else{
            val view = LayoutInflater.from(mContext).inflate(R.layout.item_left, parent, false)
            return ViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: ChatList = ChatList[position]
        holder.txtUserName.text = chat.message

        // Obtendo a referência do usuário associado ao chat atual
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(chat.senderId!!)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)

                // Verificando se o usuário não é nulo e se ele tem uma imagem de perfil
                if (user != null && !user.getImage().isNullOrEmpty()) {
                    // Carregando a imagem de perfil usando Picasso
                    Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.userProfileImage)
                } else {
                    // Usando uma imagem padrão se o usuário não tiver uma imagem de perfil
                    holder.userProfileImage.setImageResource(R.drawable.profile)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Tratamento de erro, se necessário
            }
        })
    }
    override fun getItemCount(): Int {
        return ChatList.size
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtUserName: TextView = itemView.findViewById(R.id.tvMessage)
        val userProfileImage: CircleImageView = itemView.findViewById(R.id.userImage)

    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (ChatList[position].senderId == firebaseUser!!.uid){
            return MESSAGE_TYPE_RIGHT
        }else{
            return MESSAGE_TYPE_LEFT
        }
    }
}
