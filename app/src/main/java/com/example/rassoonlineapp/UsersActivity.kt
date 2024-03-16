package com.example.rassoonlineapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.ChatAdapter
import com.example.rassoonlineapp.Model.Chat
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UsersActivity : AppCompatActivity() {

    private lateinit var imgProfile: CircleImageView
    private val chatList = ArrayList<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref",Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this@UsersActivity) { token ->
            FirebaseService.token = token
        }


        imgProfile = findViewById(R.id.imgProfile)
        findViewById<RecyclerView>(R.id.userRecyclerView).layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        findViewById<ImageView>(R.id.imgBack).setOnClickListener {
            val fragmentManager = supportFragmentManager

            if (fragmentManager.backStackEntryCount > 0) {
                fragmentManager.popBackStack()
            } else {
                finish()
            }
        }

        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        getUsersList()
    }

    private fun getUsersList() {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        var userid = firebaseUser!!.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user: User? = dataSnapshot.getValue(User::class.java)

                    if (user != null && user.getUID() != firebaseUser?.uid) {
                        val chat = Chat(user.getUsername(), user.getImage(), user.getUID())
                        chatList.add(chat)
                    }
                }

                val chatAdapter = ChatAdapter(this@UsersActivity, chatList)
                findViewById<RecyclerView>(R.id.userRecyclerView).adapter = chatAdapter

                // Carregar a imagem de perfil do usuário logado usando Picasso

                firebaseUser?.let { user ->
                    val currentUserImageURL = snapshot.child(user.uid).child("image").getValue(String::class.java)
                    currentUserImageURL?.let {
                        Picasso.get()
                            .load(it) // URL da imagem de perfil do usuário
                            .placeholder(R.drawable.profile) // Imagem de placeholder enquanto a imagem é carregada
                            .into(imgProfile) // ID da ImageView onde a imagem será carregada
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}

