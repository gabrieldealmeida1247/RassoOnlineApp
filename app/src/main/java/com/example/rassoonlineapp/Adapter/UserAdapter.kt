package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private var mContext:Context,
    private  var mUser:List<User>,
    private var isFragment: Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUser[position]

        holder.userNameTextView.text = user.getUsername()
        holder.userFullnameTextView.text = user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.userProfileImage)

    }

    override fun getItemCount(): Int {
        return mUser.size
    }

        class ViewHolder (@NonNull itemView: View) : RecyclerView.ViewHolder(itemView){
            var userNameTextView: TextView = itemView.findViewById(R.id.user_name_search)
            var userFullnameTextView: TextView = itemView.findViewById(R.id.user_full_name_search)
            var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_search)
            var dealButton: Button = itemView.findViewById(R.id.deal_btn_search)
            var totalProjects: TextView = itemView.findViewById(R.id.total_projects)
            var profileData: TextView = itemView.findViewById(R.id.textView_profile_data)

        }

}