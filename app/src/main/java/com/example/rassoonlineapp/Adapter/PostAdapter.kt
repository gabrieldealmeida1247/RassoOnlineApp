package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Post
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

class PostAdapter(
    private val mContext: Context,
    private val mPost: List<Post>
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]

        // Carrega as informações do usuário no PostAdapter
        loadUserData(post.userId.toString(), post, holder)

        // Preenche os campos no layout do post
        holder.dateHour.text = post.data_hora
        holder.tittle.text = post.titulo
        holder.description.text = post.descricao
        holder.skills.text = post.habilidades?.joinToString(", ") ?: ""
        holder.work.text = post.tipoTrabalho
        holder.orcamento.text = post.orcamento
        holder.prazo.text = post.prazo

        // Adicione um listener para abrir detalhes do post ao clicar
        holder.itemView.setOnClickListener {
            // Implemente aqui a ação desejada ao clicar em um post, como abrir uma nova atividade
            // ou fragmento para mostrar detalhes do post.
            // Por exemplo:
            // val intent = Intent(mContext, DetalhesPostActivity::class.java)
            // intent.putExtra("postId", post.postId)
            // mContext.startActivity(intent)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_post)
        var userName: TextView = itemView.findViewById(R.id.user_name_post)
        var dateHour: TextView = itemView.findViewById(R.id.data_hora)
        var tittle: TextView = itemView.findViewById(R.id.textView_tittle)
        var description: TextView = itemView.findViewById(R.id.textView_description)
        var skills: TextView = itemView.findViewById(R.id.textView_skills)
        var work: TextView = itemView.findViewById(R.id.textView_work)
        var orcamento: TextView = itemView.findViewById(R.id.textView_preco)
        var prazo: TextView = itemView.findViewById(R.id.textView_prazo)
    }

    internal fun loadUserData(userId: String, post: Post, holder: ViewHolder) {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    // Adiciona o nome de usuário e a imagem do usuário ao objeto Post
                    post.userName = user?.getUsername()
                    post.userProfileImage = user?.getImage()

                    // Verifica se o URL da imagem não é nulo ou vazio antes de carregá-lo
                    val userProfileImage = user?.getImage()
                    if (!userProfileImage.isNullOrBlank()) {
                        Picasso.get().load(userProfileImage).placeholder(R.drawable.profile)
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
