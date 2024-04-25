package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.View.ChatActivity
import com.example.rassoonlineapp.Model.Chat
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(private val mContext: Context, private val mChatList: List<Chat>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position]

        holder.userNameTextView.text = chat.userName


        // Carregar a imagem do perfil se necessário (dependendo da sua lógica de dados)
         Picasso.get().load(chat.userProfileImage).placeholder(R.drawable.profile).into(holder.userProfileImage)

        holder.layoutUser.setOnClickListener{
            // Criar um Intent para iniciar a ChatActivity
            val intent = Intent(mContext, ChatActivity::class.java)

            // Adicionar quaisquer extras necessários ao Intent, por exemplo, o ID do usuário
            intent.putExtra("userId", chat.userId)
            intent.putExtra("userName", chat.userName)

            // Iniciar a atividade usando o Intent
            mContext.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTextView: TextView = itemView.findViewById(R.id.userName)
        val txtTemp: TextView = itemView.findViewById(R.id.temp)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.userImage)
        var layoutUser: LinearLayout = itemView.findViewById(R.id.layoutUser)
    }
}
