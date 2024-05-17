package com.example.rassoonlineapp.Admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rassoonlineapp.Admin.model.BannedUser
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsersFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var userNameEditText: EditText
   // private lateinit var reasonEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize the Firebase database reference
        database = FirebaseDatabase.getInstance().reference

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_users, container, false)

        // Find views
        userNameEditText = view.findViewById(R.id.textView_user_banned)
    //    reasonEditText = view.findViewById(R.id.textView_reason)
        val banButton: Button = view.findViewById(R.id.ban_button)

        // Set click listener for ban button
        banButton.setOnClickListener {
            val userName = userNameEditText.text.toString()
         //   val reason = reasonEditText.text.toString()

            findUserIdByName(userName) { userId ->
                if (userId != null) {
                    banUser(userId)
                    incrementBannedUserCount()
                } else {
                    Toast.makeText(context, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    private fun findUserIdByName(userName: String, callback: (String?) -> Unit) {
        val usersRef = database.child("Users")
        val query = usersRef.orderByChild("username").equalTo(userName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(User::class.java)
                        // Assuming each username is unique, so we only get the first user found
                        callback(user?.getUID())
                        return
                    }
                }
                callback(null) // User not found
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }

    private fun banUser(userId: String) {
        // Store the banned user in the Firebase Realtime Database
        val bannedUsersRef = database.child("banned_users")
        val bannedUser = BannedUser(userId)

        bannedUsersRef.push().setValue(bannedUser)
            .addOnSuccessListener {
                // Ban successful
                Toast.makeText(context, "Usuário banido com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Ban failed
                Toast.makeText(context, "Falha ao banir usuário", Toast.LENGTH_SHORT).show()
            }
    }

    private fun incrementBannedUserCount() {
        val userCountRef = database.child("UserCount")
        userCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userCount = dataSnapshot.child("bannedUserCount").getValue(Int::class.java) ?: 0
                    userCountRef.child("bannedUserCount").setValue(userCount + 1)
                } else {
                    // UserCount node doesn't exist, create it and set bannedUserCount to 1
                    val newUserCount = HashMap<String, Any>()
                    newUserCount["bannedUserCount"] = 1
                    userCountRef.setValue(newUserCount)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

}
