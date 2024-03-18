package com.example.rassoonlineapp.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Constants.Constants
import com.example.rassoonlineapp.DealActivity
import com.example.rassoonlineapp.Fragments.ProfileFragment
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private var mContext:Context,
    private  var mUser:List<User>,
    private var isFragment: Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {

        val user = mUser[position]


        holder.userNameTextView.text = user.getUsername()
        holder.userFullnameTextView.text = user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
            .into(holder.userProfileImage)


        holder.itemView.setOnClickListener(View.OnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", user.getUID())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        })

        holder.dealButton.setOnClickListener {
            // Coloque o código aqui para abrir a ProposalsActivity como uma sobreposição
            val intent = Intent(mContext, DealActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            (mContext as Activity).startActivityForResult(intent, Constants.DEAL_REQUEST_CODE)
        }

    }
    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTextView: TextView = itemView.findViewById(R.id.user_name_search)
        var userFullnameTextView: TextView = itemView.findViewById(R.id.user_full_name_search)
        var userProfileImage: CircleImageView =
            itemView.findViewById(R.id.user_profile_image_search)
        var dealButton: Button = itemView.findViewById(R.id.deal_btn_search)
        var totalProjects: TextView = itemView.findViewById(R.id.total_projects)
        var profileData: TextView = itemView.findViewById(R.id.textView_profile_data)
    //    var especialidade: TextView = itemView.findViewById(R.id.function)

    }
}
