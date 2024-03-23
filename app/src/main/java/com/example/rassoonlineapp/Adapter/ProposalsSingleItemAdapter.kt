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
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ProposalsSingleItemAdapter(private val proposalsList: List<Proposals>) : RecyclerView.Adapter<ProposalsSingleItemAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.proposals_receive_item_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Aqui você pode definir os dados para os elementos do layout, se necessário

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val proposal = proposalsList[position]

        // Carrega as informações do usuário no PostAdapter
        loadProposalData(proposal.userId.toString(), proposal, holder)

        holder.descricaoTextView.text = proposal.descricao
        holder.lanceTextView.text = proposal.lance
        holder.numberDay.text = proposal.numberDays
        holder.tittle.text = proposal.projectTitle // Exibe
    }

    override fun getItemCount(): Int {
        // Neste caso, estamos exibindo apenas um item
        return proposalsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Aqui você pode inicializar os elementos do layout, se necessário

        val userProfileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val descricaoTextView: TextView = itemView.findViewById(R.id.textView_description_proposals)
        val lanceTextView: TextView = itemView.findViewById(R.id.textView_bid)
        val numberDay: TextView = itemView.findViewById(R.id.textView_number_day)
        val textView_rating: TextView = itemView.findViewById(R.id.number_rating)
        val tittle: TextView = itemView.findViewById(R.id.textView_titulo_propostas)

    }


    fun loadProposalData(userId: String, proposals: Proposals, holder: ViewHolder) {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    // Adiciona o nome de usuário e a imagem do usuário ao objeto Post
                    proposals.username = user?.getUsername()
                    proposals. profileImage = user?.getImage()

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

}
