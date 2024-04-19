package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.support.annotation.NonNull
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rassoonlineapp.Model.Notification
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(private val mContext: Context, private val mNotification: List<Notification>):
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notifications_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  mNotification.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotification[position]

        // Carrega as informações do usuário no NotificationAdapter
        holder.postTitle.text = notification.postTitle
        holder.userName.text = notification.userName

        // Carrega a imagem de perfil e o userName do post
        if (!notification.userId.isNullOrEmpty()) { // Assumindo que você tem o userId no objeto Notification
            loadUserData(notification.userId, notification, holder)
        }

        // Carregar a imagem usando Glide
        if (!notification.userProfileImage.isNullOrEmpty()) {
            Glide.with(mContext)
                .load(notification.userProfileImage)
                .placeholder(R.drawable.profile) // imagem de placeholder enquanto carrega
                .error(R.drawable.logo_rasso) // imagem de erro caso a imagem não carregue
                .into(holder.profileImage)
        }

    }


    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){

        var profileImage: CircleImageView
        var userName: TextView
        var postTitle: TextView

        init {

            profileImage = itemView.findViewById(R.id.notification_profile_image)
            userName = itemView.findViewById(R.id.username_notification)
            postTitle = itemView.findViewById(R.id.proposals_notification)
        }

    }



    private fun loadUserData(userId: String, post: Notification, holder: ViewHolder) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        // Usando addValueEventListener para ouvir as mudanças em tempo real
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    // Atualiza o nome de usuário e a imagem de perfil no objeto Notification
                    post.userName = user?.getUsername()
                    post.userProfileImage = user?.getImage()

                    // Verifica se o URL da imagem não é nulo ou vazio antes de carregá-lo
                    val userProfileImage = user?.getImage()
                    if (!userProfileImage.isNullOrBlank()) {
                        Glide.with(mContext)
                            .load(userProfileImage)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.logo_rasso)
                            .into(holder.profileImage)
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