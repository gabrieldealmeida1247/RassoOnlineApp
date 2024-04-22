package com.example.rassoonlineapp

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rassoonlineapp.Adapter.ChatListAdapter
import com.example.rassoonlineapp.Model.ChatList
import com.example.rassoonlineapp.Model.NotificationData
import com.example.rassoonlineapp.Model.PushNotification
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R.layout
import com.example.rassoonlineapp.WorkManager.NotificationWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class ChatActivity : AppCompatActivity() {
    private lateinit var imgProfile: CircleImageView
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var chatList = ArrayList<ChatList>()
    var topic = ""

    // Dentro da classe ChatActivity
    private lateinit var coroutineScope: CoroutineScope
    private val job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_chat)

        findViewById<RecyclerView>(R.id.chatRecyclerView).layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Inicialize a variável imgProfile
        imgProfile = findViewById(R.id.imgProfile)

        findViewById<ImageView>(R.id.imgBack).setOnClickListener {
            onBackPressed()
        }

        var intent = intent
        var userId = intent.getStringExtra("userId")
        var userName = intent.getStringExtra("userName")

        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                findViewById<TextView>(R.id.tvUserName).text = user!!.getUsername()

                if (user.getImage().isNullOrEmpty()){
                    // Se a URL da imagem estiver vazia ou nula, carregue a imagem padrão
                    imgProfile.setImageResource(R.drawable.profile)
                } else {
                    // Caso contrário, carregue a imagem usando Picasso
                    Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(imgProfile)
                }

                findViewById<ImageButton>(R.id.btnSendMessage).setOnClickListener {
                    var message: String = findViewById<EditText>(R.id.etMessage).text.toString()

                    if (message.isEmpty()) {
                        Toast.makeText(applicationContext, "message is empty", Toast.LENGTH_SHORT).show()
                    } else {
                        sendMessage(firebaseUser!!.uid, userId, message)
                        findViewById<EditText>(R.id.etMessage).setText("")
                        topic = "/topics/$userId"
                        PushNotification(NotificationData(userName!!, message),
                            topic).also {
                            sendNotification(it)
                        }
                    }

                }

            }

            override fun onCancelled(error: DatabaseError){

            }

        })

        readMessage(userId, firebaseUser!!.uid)

        coroutineScope = CoroutineScope(Dispatchers.Main + job)
    }



    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        var reference: DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        var hashMap: HashMap<String, String> = HashMap()
        hashMap["senderId"] = senderId
        hashMap["receiverId"] = receiverId
        hashMap["message"] = message

        reference!!.child("ChatList").push().setValue(hashMap)

        // Dentro do método sendMessage() após enviar a mensagem
        val notificationData = NotificationData("", message)
        val pushNotification = PushNotification(notificationData, topic)
        val gson = Gson()
        val notificationJson = gson.toJson(pushNotification)

        val inputData = Data.Builder()
            .putString("notification", notificationJson)
            .build()

        WorkManager.getInstance(this).enqueue(
            OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(inputData)
                .build()
        )


    }

    fun readMessage(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("ChatList")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(ChatList::class.java)

                    if (chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId) ||
                        chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)
                    ) {
                        chatList.add(chat)
                    }
                }

                val chatListAdapter = ChatListAdapter(this@ChatActivity, chatList)

                findViewById<RecyclerView>(R.id.chatRecyclerView).adapter = chatListAdapter
            }
        })
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d("TAG", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("TAG", response.errorBody()!!.string())
            }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancela todas as coroutines quando a activity é destruída
    }
}

