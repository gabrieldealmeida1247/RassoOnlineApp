package com.example.rassoonlineapp.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Rating
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
// RatingItemAdapter.kt

class RatingItemAdapter(private val context: Context, private val ratingList: MutableList<Rating>) :
    RecyclerView.Adapter<RatingItemAdapter.RatingViewHolder>() {

    private lateinit var usersReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    init {
        firebaseAuth = FirebaseAuth.getInstance()
        usersReference = FirebaseDatabase.getInstance().reference.child("Users")
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rating_item_layout, parent, false)
        return RatingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        val rating = ratingList[position]
        holder.nameTextView.text = "${rating.userName}"
        holder.descriptionTextView.text = "${rating.description}"
        holder.ratingBar.rating = rating.rating.toFloat()

        // Carregar a imagem de perfil usando userId
        val userId = rating.userId
        if (!userId.isNullOrEmpty()) {
            usersReference.child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfileImageUrl = snapshot.child("image").value.toString()
                        if (userProfileImageUrl.isNotEmpty()) {
                            Picasso.get().load(userProfileImageUrl).into(holder.profileImageView)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Tratar erros aqui
                }
            })
        }
    }
    override fun getItemCount(): Int {
        return ratingList.size
    }

    inner class RatingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val profileImageView: CircleImageView = itemView.findViewById(R.id.image_profile_rating_item)
         val nameTextView: TextView = itemView.findViewById(R.id.text_view_name_item)
         val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar_item)
         val descriptionTextView: TextView = itemView.findViewById(R.id.text_rating_item)

    }
}