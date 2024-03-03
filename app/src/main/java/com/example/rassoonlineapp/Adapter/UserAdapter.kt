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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

        checkDealStatus(user.getUID(), holder.dealButton)

        holder.dealButton.setOnClickListener {
            if (holder.dealButton.text.toString() == "Contratar") {

                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Contratar").child(it1.toString())
                        .child("Contrating").child(user.getUID())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Contratar").child(user.getUID())
                                        .child("Workers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }

            }
            else {


                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Contratar").child(it1.toString())
                        .child("Contrating").child(user.getUID())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Contratar").child(user.getUID())
                                        .child("Workers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }

            }

        }

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

    private fun checkDealStatus(uid: String, dealButton: Button) {

        val dealingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Contratar").child(it1.toString())
                .child("Contrating")

        }
        dealingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if (datasnapshot.child(uid).exists()){
                    dealButton.text = "Contrating"
                }else{
                    dealButton.text = "Contratar"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        } )
    }

}