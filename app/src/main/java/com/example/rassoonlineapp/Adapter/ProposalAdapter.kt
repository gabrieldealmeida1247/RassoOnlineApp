package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class ProposalAdapter( private val context: Context,
                       private val proposalsList: List<Proposals>
) :
    RecyclerView.Adapter<ProposalAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.proposals_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val proposal = proposalsList[position]
 /*
        // Carrega as informações do usuário no PostAdapter
        loadProposalData(proposal.userId.toString(), proposal, holder)

        holder.descricaoTextView.text = proposal.descricao
        holder.lanceTextView.text = proposal.lance
        holder.numberDay.text = proposal.numberDays
        holder.tittle.text = proposal.projectTitle // Exibe

  */

        // Se o ID do usuário associado à proposta for igual ao ID do usuário atual, exiba a proposta
        if (firebaseUser?.uid == proposal.userId) {
            // Carrega as informações do usuário no PostAdapter
            loadProposalData(proposal.userId.toString(), proposal, holder)

            holder.descricaoTextView.text = proposal.descricao
            holder.lanceTextView.text = proposal.lance
            holder.numberDay.text = proposal.numberDays
            holder.tittle.text = proposal.projectTitle // Exibe
           // holder.textRecusado.text = proposal.rejected
          //  holder.textRecusado.setTextColor(ContextCompat.getColor(context, R.color.red))
            // Verifica se a proposta foi aceita
            if (proposal.accepted == "Aprovado") {
                holder.textStatus.text = "Aprovado"
                holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
            } else if (proposal.rejected == "Reprovado") {
                holder.textStatus.text = "Reprovado"
                holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
            } else {
                holder.textStatus.text = "Pendente"
                holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.colorOrange))
            }
        } else {
            // Se o ID do usuário não corresponder, oculte a visibilidade do item
            holder.itemView.visibility = View.GONE
            // Defina a altura do item como 0 para evitar espaços vazios na lista
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        }



    }

    override fun getItemCount(): Int {
        return proposalsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userProfileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val descricaoTextView: TextView = itemView.findViewById(R.id.textView_description_proposals)
        val lanceTextView: TextView = itemView.findViewById(R.id.textView_bid)
        val numberDay: TextView = itemView.findViewById(R.id.textView_number_day)
        val textView_rating: TextView = itemView.findViewById(R.id.number_rating)
        val tittle: TextView = itemView.findViewById(R.id.textView_titlle_proposols)
        val textStatus: TextView = itemView.findViewById(R.id.textView_aceite)
    }

    fun loadProposalData(userId: String, proposals: Proposals, holder: ProposalAdapter.ViewHolder) {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    // Adiciona o nome de usuário e a imagem do usuário ao objeto Post
                    proposals.username = user?.getUsername()
                    proposals.profileImage = user?.getImage()

                    // Verifica se o URL da imagem não é nulo ou vazio antes de carregá-lo
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

    // Função para excluir uma proposta pelo ID do post associado
    fun deleteProposalByPostId(postId: String) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Proposals")

        // Query para encontrar propostas associadas ao post pelo postId
        val query = databaseReference.orderByChild("postId").equalTo(postId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    // Remove a proposta do banco de dados
                    snapshot.ref.removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}