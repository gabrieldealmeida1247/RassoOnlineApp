package com.example.rassoonlineapp.Adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Admin.model.ServiceCount
import com.example.rassoonlineapp.Constants.Constants.Companion.PROPOSAL_REQUEST_CODE
import com.example.rassoonlineapp.Model.Post
import com.example.rassoonlineapp.Model.Statistic
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.EditPostActivity
import com.example.rassoonlineapp.View.InaproprieteContentActivity
import com.example.rassoonlineapp.View.ProposalsActivity
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
    private val mPost: List<Post>,   private val showProposalButton: Boolean = true
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

        if (showProposalButton) {
            holder.btnFazerProposta.visibility = View.VISIBLE
        } else {
            holder.btnFazerProposta.visibility = View.GONE
        }

        // Verifica se o usuário atual é o dono do post e ajusta a visibilidade do botão de deletar
        if (firebaseUser?.uid == post.userId) {
            holder.deletePost.visibility = View.VISIBLE
        } else {
            holder.deletePost.visibility = View.GONE
        }

        // Verifica se o usuário atual é o dono do post e ajusta a visibilidade do botão de deletar
        if (firebaseUser?.uid == post.userId) {
            holder.editPost.visibility = View.VISIBLE
        } else {
            holder.editPost.visibility = View.GONE
        }


        holder.deletePost.setOnClickListener {
            // Verificar se o usuário atual é o dono do post
            if (firebaseUser?.uid == post.userId) {
                showDeleteConfirmationDialog(post.postId!!)
            } else {
                Toast.makeText(mContext, "Você não pode deletar este post.", Toast.LENGTH_SHORT).show()
            }
        }


        // Preenche os campos no layout do post
        holder.dateHour.text = post.data_hora
        holder.tittle.text = post.titulo
        holder.description.text = post.descricao
        holder.skills.text = post.habilidades?.joinToString(", ") ?: ""
      //  holder.work.text = post.tipoTrabalho
        holder.local.text = post.local
        holder.orcamento.text = post.orcamento
        holder.prazo.text = post.prazo

        holder.editPost.setOnClickListener {
            if (firebaseUser?.uid == post.userId) {
                // O usuário pode fazer uma proposta
                val intent = Intent(mContext, EditPostActivity::class.java)
                intent.putExtra("postId", post.postId) // Passa o ID do projeto para a ProposalsActivity
                intent.putExtra("projectTitle", post.titulo) // Passa o título do projeto para a ProposalsActivity
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                (mContext as Activity).startActivityForResult(intent, PROPOSAL_REQUEST_CODE)
            } else {
                // Exibe uma mensagem informando que o usuário não pode fazer uma proposta em seu próprio projeto
                Toast.makeText(mContext, "Você não pode fazer uma proposta em seu próprio projeto.", Toast.LENGTH_SHORT).show()
            }
        }
        holder.btnFazerProposta.setOnClickListener {
            // Verificar se o usuário atual é diferente do usuário que publicou o projeto
            if (firebaseUser?.uid != post.userId) {
                // O usuário pode fazer uma proposta
                val intent = Intent(mContext, ProposalsActivity::class.java)
                intent.putExtra("postId", post.postId) // Passa o ID do projeto para a ProposalsActivity
                intent.putExtra("projectTitle", post.titulo) // Passa o título do projeto para a ProposalsActivity
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                (mContext as Activity).startActivityForResult(intent, PROPOSAL_REQUEST_CODE)
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
                (mContext as Activity).startActivityForResult(intent, PROPOSAL_REQUEST_CODE)
            } else {
                // Exibe uma mensagem informando que o usuário não pode fazer uma proposta em seu próprio projeto
                Toast.makeText(mContext, "Você não pode  denúnciar seu próprio projeto.", Toast.LENGTH_SHORT).show()
            }
        }

        // Verifica o status da proposta
        checkProposalStatus(post.postId!!, holder.btnFazerProposta)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_post)
        var userName: TextView = itemView.findViewById(R.id.user_name_post)
        var dateHour: TextView = itemView.findViewById(R.id.data_hora)
        var tittle: TextView = itemView.findViewById(R.id.textView_tittle)
        var description: TextView = itemView.findViewById(R.id.textView_description)
        var skills: TextView = itemView.findViewById(R.id.textView_skills)
       // var work: TextView = itemView.findViewById(R.id.textView_work)
        var local: TextView = itemView.findViewById(R.id.textView_local)
        var orcamento: TextView = itemView.findViewById(R.id.textView_preco)
        var prazo: TextView = itemView.findViewById(R.id.textView_prazo)
        var btnFazerProposta = itemView.findViewById<AppCompatButton>(R.id.btn_fazer_proposta)
        var inapropriateContent = itemView.findViewById<TextView>(R.id.textView_inapropriete_content)
        var deletePost = itemView.findViewById<ImageView>(R.id.delete_post)
        var editPost = itemView.findViewById<ImageView>(R.id.edit_post)
    }

    private fun loadUserData(userId: String, post: Post, holder: ViewHolder) {
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



    private fun showDeleteConfirmationDialog(postId: String) {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("Confirmar Exclusão")
        builder.setMessage("Você tem certeza que deseja deletar este post?")
        builder.setPositiveButton("Sim") { dialog, _ ->
            deletePost(postId)
            dialog.dismiss()
        }
        builder.setNegativeButton("Não") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deletePost(postId: String) {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
        postRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Atualiza a contagem de serviços deletados
                // Obtém o ID do usuário atual
                val currentUserID = firebaseUser?.uid ?: ""
                updateServiceDeleteCount(postId, currentUserID)
                updateServiceDeleteCount()
                Toast.makeText(mContext, "Post deletado com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext, "Erro ao deletar o post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateServiceDeleteCount(postId: String, userId: String) {
        val statsRef = FirebaseDatabase.getInstance().reference.child("Statistics").child(userId)

        statsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val statistic = dataSnapshot.getValue(Statistic::class.java)
                    if (statistic != null && userId == statistic.userId) {
                        val newDeleteCount = statistic.servicesDeleted + 1
                        statsRef.child("servicesDeleted").setValue(newDeleteCount)
                            .addOnSuccessListener {
                                Log.d("PostAdapter", "Service delete count updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("PostAdapter", "Failed to update service delete count: ${e.message}")
                            }
                    } else {
                        Log.e("PostAdapter", "Statistic does not exist or user is not the owner")
                    }
                } else {
                    Log.e("PostAdapter", "Statistic does not exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PostAdapter", "Database query cancelled: ${databaseError.message}")
            }
        })
    }

    private fun updateServiceDeleteCount() {
        val statsRef = FirebaseDatabase.getInstance().reference.child("ServiceCount")

        statsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val statistic = dataSnapshot.getValue(ServiceCount::class.java)
                    if (statistic != null) {
                        val newDeleteCount = statistic.deleteCount + 1
                        statsRef.child("deleteCount").setValue(newDeleteCount)
                            .addOnSuccessListener {
                                Log.d("PostAdapter", "Service delete count updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("PostAdapter", "Failed to update service delete count: ${e.message}")
                            }
                    } else {
                        Log.e("PostAdapter", "Statistic does not exist")
                    }
                } else {
                    Log.e("PostAdapter", "Statistic does not exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PostAdapter", "Database query cancelled: ${databaseError.message}")
            }
        })
    }


}
