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
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.Model.User
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
    }


    /*
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

     */

    /*
    private fun getUsersList() {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val currentUserId = firebaseUser?.uid ?: return

        // Referência para Proposals
        val proposalsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Proposals")

        // Lista para armazenar userIdOther dos Proposals
        val userIdOthersList = ArrayList<String>()

        // Consulta para obter userIdOther dos Proposals
        proposalsRef.orderByChild("userId").equalTo(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userIdOthersList.clear()

                for (dataSnapshot in snapshot.children) {
                    val proposal = dataSnapshot.getValue(Proposals::class.java)
                    proposal?.let {
                        userIdOthersList.add(it.userIdOther ?: "")
                    }
                }

                // Agora, com userIdOthersList, podemos filtrar os usuários
                val usersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

                usersRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(usersSnapshot: DataSnapshot) {
                        chatList.clear()

                        for (dataSnapshot: DataSnapshot in usersSnapshot.children) {
                            val user: User? = dataSnapshot.getValue(User::class.java)

                            if (user != null && user.getUID() != currentUserId && userIdOthersList.contains(user.getUID())) {
                                val chat = Chat(user.getUsername(), user.getImage(), user.getUID())
                                chatList.add(chat)

                            }

                        }

                        val chatAdapter = ChatAdapter(this@UsersActivity, chatList)
                        findViewById<RecyclerView>(R.id.userRecyclerView).adapter = chatAdapter

                        // Carregar a imagem de perfil do usuário logado usando Picasso
                        val currentUserImageURL = usersSnapshot.child(currentUserId).child("image").getValue(String::class.java)
                        currentUserImageURL?.let {
                            Picasso.get()
                                .load(it)
                                .placeholder(R.drawable.profile)
                                .into(imgProfile)
                        }
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

 */

    /*
    private fun getUsersList() {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val currentUserId = firebaseUser?.uid ?: return

        // Referência para Proposals
        val proposalsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Proposals")

        // Lista para armazenar userIdOther dos Proposals
        val userIdOthersList = ArrayList<String>()

        // Consulta para obter userIdOther dos Proposals
        proposalsRef.orderByChild("userId").equalTo(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userIdOthersList.clear()

                for (dataSnapshot in snapshot.children) {
                    val proposal = dataSnapshot.getValue(Proposals::class.java)
                    proposal?.let {
                        userIdOthersList.add(it.userIdOther ?: "")
                    }
                }

                // Agora, com userIdOthersList, podemos filtrar os usuários
                val usersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

                usersRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(usersSnapshot: DataSnapshot) {
                        chatList.clear()

                        // Carregar o usuário logado
                        val currentUser = usersSnapshot.child(currentUserId).getValue(User::class.java)
                        currentUser?.let {
                            val chat = Chat(it.getUsername(), it.getImage(), it.getUID())
                            chatList.add(chat)
                        }

                        // Carregar os outros usuários
                        for (dataSnapshot: DataSnapshot in usersSnapshot.children) {
                            val user: User? = dataSnapshot.getValue(User::class.java)

                            if (user != null && user.getUID() != currentUserId && userIdOthersList.contains(user.getUID())) {
                                val chat = Chat(user.getUsername(), user.getImage(), user.getUID())
                                chatList.add(chat)
                            }
                        }

                        val chatAdapter = ChatAdapter(this@UsersActivity, chatList)
                        findViewById<RecyclerView>(R.id.userRecyclerView).adapter = chatAdapter

                        // Carregar a imagem de perfil do usuário logado usando Picasso
                        val currentUserImageURL = usersSnapshot.child(currentUserId).child("image").getValue(String::class.java)
                        currentUserImageURL?.let {
                            Picasso.get()
                                .load(it)
                                .placeholder(R.drawable.profile)
                                .into(imgProfile)
                        }
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

 */

    /*
    private fun getUsersList() {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        var userid = firebaseUser!!.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    val userId = user?.getUID()

                    if (user != null && userId != firebaseUser?.uid) {
                        val isProposalAccepted = sharedPref.getBoolean("proposalAccepted_$userId", false)

                        if (isProposalAccepted) {
                            val chat = Chat(user.getUsername(), user.getImage(), user.getUID())
                            chatList.add(chat)
                        }
                    }
                }

                val chatAdapter = ChatAdapter(this@UsersActivity, chatList)
                findViewById<RecyclerView>(R.id.userRecyclerView).adapter = chatAdapter

                firebaseUser?.let { user ->
                    val currentUserImageURL = snapshot.child(user.uid).child("image").getValue(String::class.java)
                    currentUserImageURL?.let {
                        Picasso.get()
                            .load(it)
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

 */

    /*
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
        val sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        val chatList = ArrayList<Chat>()

        // Query to get userIdOther from Proposals where userId == userIdOther
        proposalsRef.orderByChild("userIdOther").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(proposalsSnapshot: DataSnapshot) {
                for (dataSnapshot in proposalsSnapshot.children) {
                    val proposal = dataSnapshot.getValue(Proposals::class.java)
                    proposal?.let { prop ->
                        val userIdOther = prop.userId

                        // Fetch user details from Users node for userIdOther
                        usersRef.child(userIdOther!!).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(User::class.java)
                                user?.let {
                                    val chat = Chat(it.getUsername(), it.getImage(), userIdOther)
                                    chatList.add(chat)
                                }

                                // Update RecyclerView adapter after adding user
                                val chatAdapter = ChatAdapter(this@UsersActivity, chatList)
                                findViewById<RecyclerView>(R.id.userRecyclerView).adapter = chatAdapter
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        // Query to get userId from Proposals where userIdOther == userId
        proposalsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(proposalsSnapshot: DataSnapshot) {
                for (dataSnapshot in proposalsSnapshot.children) {
                    val proposal = dataSnapshot.getValue(Proposals::class.java)
                    proposal?.let { prop ->
                        val userIdAccepted = prop.userIdOther

                        // Fetch user details from Users node for userIdAccepted
                        usersRef.child(userIdAccepted!!).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(acceptedUserSnapshot: DataSnapshot) {
                                val acceptedUser = acceptedUserSnapshot.getValue(User::class.java)
                                acceptedUser?.let {
                                    val chat = Chat(it.getUsername(), it.getImage(), userIdAccepted)
                                    chatList.add(chat)
                                }

                                // Update RecyclerView adapter after adding user
                                val chatAdapter = ChatAdapter(this@UsersActivity, chatList)
                                findViewById<RecyclerView>(R.id.userRecyclerView).adapter = chatAdapter
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

     */

/*
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

        // Fetch users who made proposals to the current user
        proposalsRef.orderByChild("userIdOther").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(proposalsSnapshot: DataSnapshot) {
                for (dataSnapshot in proposalsSnapshot.children) {
                    val proposal = dataSnapshot.getValue(Proposals::class.java)
                    proposal?.let { prop ->
                        val userIdOther = prop.userId

                        // Fetch user details from Users node for userIdOther
                        usersRef.child(userIdOther!!).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(User::class.java)
                                user?.let {
                                    val chat = Chat(it.getUsername(), it.getImage(), userIdOther)
                                    chatList.add(chat)
                                }

                                // Update RecyclerView adapter after fetching all users
                                updateRecyclerViewAdapter(chatList)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch users who accepted proposals from the current user
        proposalsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(proposalsSnapshot: DataSnapshot) {
                for (dataSnapshot in proposalsSnapshot.children) {
                    val proposal = dataSnapshot.getValue(Proposals::class.java)
                    proposal?.let { prop ->
                        val userIdAccepted = prop.userIdOther

                        // Fetch user details from Users node for userIdAccepted
                        usersRef.child(userIdAccepted!!).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(acceptedUserSnapshot: DataSnapshot) {
                                val acceptedUser = acceptedUserSnapshot.getValue(User::class.java)
                                acceptedUser?.let {
                                    val chat = Chat(it.getUsername(), it.getImage(), userIdAccepted)
                                    chatList.add(chat)
                                }

                                // Update RecyclerView adapter after fetching all users
                                updateRecyclerViewAdapter(chatList)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRecyclerViewAdapter(chatList: ArrayList<Chat>) {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        // Update RecyclerView adapter
        val chatAdapter = ChatAdapter(this@UsersActivity, chatList)
        findViewById<RecyclerView>(R.id.userRecyclerView).adapter = chatAdapter

        // Load current user's profile image
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

 */

/*
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
                        userIdList.add(userIdOther!!)
                    }
                }

                // Fetch users who accepted proposals from the current user
                proposalsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(proposalsSnapshot: DataSnapshot) {
                        for (dataSnapshot in proposalsSnapshot.children) {
                            val proposal = dataSnapshot.getValue(Proposals::class.java)
                            proposal?.let { prop ->
                                val userIdAccepted = prop.userIdOther
                                userIdList.add(userIdAccepted!!)
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

 */

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
