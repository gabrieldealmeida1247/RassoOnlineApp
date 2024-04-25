package com.example.rassoonlineapp.View

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rassoonlineapp.Adapter.ChatAdapter
import com.example.rassoonlineapp.Model.Chat
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.ViewModel.WorkManager.FetchUsersWorker
import com.example.rassoonlineapp.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

class UsersActivity : AppCompatActivity() {

    private lateinit var imgProfile: CircleImageView
    private val chatList = ArrayList<Chat>()

    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this@UsersActivity) { token ->
            FirebaseService.token = token
        }


        imgProfile = findViewById(R.id.imgProfile)
        findViewById<RecyclerView>(R.id.userRecyclerView).layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

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

        // Fetch users using WorkManager
        fetchUsersWithWorkManager()
    }


    private fun fetchUsersWithWorkManager() {
        val workRequest = OneTimeWorkRequestBuilder<FetchUsersWorker>().build()

        WorkManager.getInstance(this).enqueue(workRequest)

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.id)
            .observe(this, { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    lifecycleScope.launch {
                        if (workInfo.state == androidx.work.WorkInfo.State.SUCCEEDED) {
                            // Handle successful work completion
                            // Maybe update UI or show a Toast
                        } else {
                            // Handle failure
                            // Maybe show an error Toast
                        }
                    }
                }
            })
    }


    private fun getUsersList() {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            Toast.makeText(applicationContext, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = firebaseUser.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userId")

        val proposalsRef = FirebaseDatabase.getInstance().getReference("Proposals")
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        val chatList = ArrayList<Chat>()
        val userIdList = HashSet<String>()  // Usando HashSet para evitar duplicatas

        // Fetch users who made proposals to the current user
        proposalsRef.orderByChild("userIdOther").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(proposalsSnapshot: DataSnapshot) {
                for (dataSnapshot in proposalsSnapshot.children) {
                    val proposal = dataSnapshot.getValue(Proposals::class.java)
                    proposal?.let { prop ->
                        val userIdOther = prop.userId
                        if (proposal.accepted =="Aprovado"){
                            userIdList.add(userIdOther!!)
                        }

                    }
                }

                // Fetch users who accepted proposals from the current user
                proposalsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(proposalsSnapshot: DataSnapshot) {
                        for (dataSnapshot in proposalsSnapshot.children) {
                            val proposal = dataSnapshot.getValue(Proposals::class.java)
                            proposal?.let { prop ->
                                val userIdAccepted = prop.userIdOther
                                if (proposal.accepted == "Aprovado"){
                                    userIdList.add(userIdAccepted!!)
                                }


                            }
                        }

                        // Fetch user details from Users node for the combined userIdList
                        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(usersSnapshot: DataSnapshot) {
                                for (userId in userIdList) {
                                    val userSnapshot = usersSnapshot.child(userId)
                                    val user = userSnapshot.getValue(User::class.java)
                                    user?.let {
                                        val chat = Chat(it.getUsername(), it.getImage(), userId)
                                        chatList.add(chat)
                                    }
                                }

                                // Update RecyclerView adapter after fetching all users
                                updateRecyclerViewAdapter(chatList)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRecyclerViewAdapter(chatList: ArrayList<Chat>) {
        val chatAdapter = ChatAdapter(this@UsersActivity, chatList)
        findViewById<RecyclerView>(R.id.userRecyclerView).adapter = chatAdapter

        // Load current user's profile image
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        firebaseUser?.uid?.let { userId ->
            usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentUser = dataSnapshot.getValue(User::class.java)
                    currentUser?.let {
                        val userProfileImage = it.getImage()

                        // Load profile image into ImageView
                        userProfileImage?.let { imageUrl ->
                            Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.profile)
                                .into(imgProfile)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


}
