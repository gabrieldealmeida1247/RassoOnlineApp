package com.example.rassoonlineapp.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Constants.Constants
import com.example.rassoonlineapp.View.InaproprieteContentActivity
import com.example.rassoonlineapp.Model.Post
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.ProposalsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostSearchAdapter(private val mContext: Context, private val mPost: List<Post>,  private var isFragment: Boolean = false ): RecyclerView.Adapter<PostSearchAdapter.ViewHolder>() {
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostSearchAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_search_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostSearchAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]

        // Carrega as informações do usuário no PostAdapter
        loadUserData(post.userId.toString(), post, holder)

        holder.date.text = post.data_hora
        holder.tittle.text = post.titulo
        holder.description.text = post.descricao
        holder.work.text = post.tipoTrabalho
        holder.preco.text = post.orcamento
        holder.prazo.text = post.prazo
        holder.skills.text = post.habilidades?.joinToString(", ") ?: ""

        holder.btnFazerProposta.setOnClickListener {
            // Verificar se o usuário atual é diferente do usuário que publicou o projeto
            if (firebaseUser?.uid != post.userId) {
                // O usuário pode fazer uma proposta
                val intent = Intent(mContext, ProposalsActivity::class.java)
                intent.putExtra("postId", post.postId) // Passa o ID do projeto para a ProposalsActivity
                intent.putExtra("projectTitle", post.titulo) // Passa o título do projeto para a ProposalsActivity
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                (mContext as Activity).startActivityForResult(intent,
                    Constants.PROPOSAL_REQUEST_CODE
                )
            } else {
                // Exibe uma mensagem informando que o usuário não pode fazer uma proposta em seu próprio projeto
                Toast.makeText(mContext, "Você não pode fazer uma proposta em seu próprio projeto.", Toast.LENGTH_SHORT).show()
            }
        }

        holder.inapropriateContent.setOnClickListener {
            // Verificar se o usuário atual é diferente do usuário que publicou o projeto
            if (firebaseUser?.uid != post.userId) {
                // O usuário pode fazer uma proposta
                val intent = Intent(mContext, InaproprieteContentActivity::class.java)
                intent.putExtra("postId", post.postId)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                (mContext as Activity).startActivityForResult(intent,
                    Constants.PROPOSAL_REQUEST_CODE
                )
            } else {
                // Exibe uma mensagem informando que o usuário não pode fazer uma proposta em seu próprio projeto
                Toast.makeText(mContext, "Você não pode  denúnciar seu próprio projeto.", Toast.LENGTH_SHORT).show()
            }
        }

        // Verifica o status da proposta
        checkProposalStatus(post.postId!!, holder.btnFazerProposta)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

   class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
       var userProfileImage: CircleImageView =
           itemView.findViewById(R.id.user_profile_image_post)
       var userName: TextView = itemView.findViewById(R.id.user_name_post)
       var date: TextView = itemView.findViewById(R.id.data_hora)
       var tittle: TextView = itemView.findViewById(R.id.textView_tittle)
       var description: TextView = itemView.findViewById(R.id.textView_description)
       var skills: TextView = itemView.findViewById(R.id.textView_skills)
       var work: TextView = itemView.findViewById(R.id.textView_work)
       var preco: TextView = itemView.findViewById(R.id.textView_preco)
       var prazo: TextView = itemView.findViewById(R.id.textView_prazo)
       var btnFazerProposta:AppCompatButton= itemView.findViewById(R.id.btn_fazer_proposta)
       var inapropriateContent = itemView.findViewById<TextView>(R.id.textView_inapropriete_content)



   }


    private fun loadUserData(userId: String, post: Post, holder: PostSearchAdapter.ViewHolder) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

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
                            .into(holder.userProfileImage)
                    }

                    holder.userName.text = user?.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


    private fun checkProposalStatus(postId: String, btnFazerProposta: AppCompatButton) {
        val proposalsRef = FirebaseDatabase.getInstance().reference.child("Proposals")

        // Query para buscar a proposta com base no postId
        val query = proposalsRef.orderByChild("postId").equalTo(postId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("checkProposalStatus", "onDataChange called")

                if (dataSnapshot.exists()) {
                    for (proposalSnapshot in dataSnapshot.children) {
                        val accepted = proposalSnapshot.child("accepted").getValue(String::class.java)

                        if (accepted != null) {
                            Log.d("checkProposalStatus", "accepted value: $accepted")

                            // Verifica o status e ajusta a visibilidade do botão
                            if ("Aprovado" == accepted) {
                                // Esconde o botão
                                btnFazerProposta.visibility = View.GONE
                            } else {
                                // Mostra o botão
                                btnFazerProposta.visibility = View.VISIBLE
                            }
                        } else {
                            Log.d("checkProposalStatus", "accepted is null")
                        }
                    }
                } else {
                    Log.d("checkProposalStatus", "DataSnapshot does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("checkProposalStatus", "onCancelled called: $error")
            }
        })
    }
}