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
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
// RatingItemAdapter.kt

class RatingItemAdapter(private val context: Context, private val ratingList: MutableList<Rating>) :
    RecyclerView.Adapter<RatingItemAdapter.RatingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rating_item_layout, parent, false)
        return RatingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        val rating = ratingList[position]
        holder.bind(rating)
    }

    override fun getItemCount(): Int {
        return ratingList.size
    }

    inner class RatingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: CircleImageView = itemView.findViewById(R.id.image_profile_rating_item)
        private val nameTextView: TextView = itemView.findViewById(R.id.text_view_name_item)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar_item)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.text_rating_item)

        fun bind(rating: Rating) {
            nameTextView.text = rating.userName
            ratingBar.rating = rating.rating.toFloat()
            descriptionTextView.text = rating.description

            // Carregar a imagem de perfil usando Picasso
            if (rating.userProfileImageUrl.isNotEmpty()) {
                Picasso.get().load(rating.userProfileImageUrl).placeholder(R.drawable.profile).into(profileImageView)
                profileImageView.visibility = View.VISIBLE
            } else {
                // Se a URL da imagem estiver vazia, ocultar a imagem de perfil
                profileImageView.visibility = View.GONE
            }
        }
    }
}
