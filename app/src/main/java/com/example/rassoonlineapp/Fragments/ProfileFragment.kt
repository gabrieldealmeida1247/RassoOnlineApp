package com.example.rassoonlineapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rassoonlineapp.AccountSettingsActivity
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inicializar o adapter com a lista mutável

        


        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        // Verificar se o perfil visualizado pertence ao usuário atual
        if (profileId == firebaseUser.uid) {
            view.findViewById<Button>(R.id.edit_account_settings_btn).text = "Edit Profile"
        } else {
            // Se não, ocultar o botão "Edit Account Settings"
            view.findViewById<Button>(R.id.edit_account_settings_btn).visibility = View.GONE
        }

        view.findViewById<Button>(R.id.edit_account_settings_btn).setOnClickListener {
            val getButtonText = view.findViewById<Button>(R.id.edit_account_settings_btn).text.toString()
            when {
                getButtonText == "Edit Profile" ->  startActivity(Intent(context, AccountSettingsActivity::class.java))
            }
        }

        userInfo()
        return view
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
            .child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile)
                        .into(view?.findViewById(R.id.pro_image_profile_frag))
                    view?.findViewById<TextView>(R.id.profile_fragment_username)?.text =
                        user?.getUsername()
                    view?.findViewById<TextView>(R.id.full_name_profile_frag)?.text =
                        user?.getFullname()
                    view?.findViewById<TextView>(R.id.bio_profile_frag)?.text = user?.getBio()
                    view?.findViewById<TextView>(R.id.textView_profile_data)?.text =
                        user?.getDescription()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

}
