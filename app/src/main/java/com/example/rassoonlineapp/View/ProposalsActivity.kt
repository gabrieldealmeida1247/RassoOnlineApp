package com.example.rassoonlineapp.View

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rassoonlineapp.Adapter.ProposalsSingleItemAdapter
import com.example.rassoonlineapp.Model.NotificationData
import com.example.rassoonlineapp.Model.Post
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.Model.ProposalsStatistic
import com.example.rassoonlineapp.Model.PushNotification
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.ViewModel.WorkManager.NotificationWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import java.util.UUID

class ProposalsActivity : AppCompatActivity(), ProposalsSingleItemAdapter.ProposalAcceptListener {
    private lateinit var adapter: ProposalsSingleItemAdapter
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var postId: String
    private lateinit var projectTitle: String
    private lateinit var description: String
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var proposalsReference: DatabaseReference
    private lateinit var postOwnerId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proposals)

        //description = FirebaseDatabase.getInstance().reference.child("Proposals").child(description).toString()

        val proposalsList: MutableList<Proposals>? = null
        if (proposalsList != null) {
            adapter = ProposalsSingleItemAdapter(proposalsList)
            adapter.setAcceptListener(this)
        } else {
            // Trate a lista nula de acordo com a lógica do seu aplicativo
        }
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()
        proposalsReference = firebaseDatabase.reference.child("Proposals")

        postId = intent.getStringExtra("postId") ?: ""
        projectTitle = intent.getStringExtra("projectTitle") ?: ""

        val bidAmountInput = findViewById<EditText>(R.id.bid_amount)
        val finalAmountSpan = findViewById<TextView>(R.id.paid_to_you)
        val deliveryTimeInput = findViewById<EditText>(R.id.delivery_time)
        val descriptionInput = findViewById<EditText>(R.id.description)
        val sendButton = findViewById<Button>(R.id.send_button)

        bidAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed to implement this method
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed to implement this method
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val bidAmount = if (it.isNotEmpty()) it.toString().toDouble() else 0.0
                    val fee = bidAmount * 0.3
                    val paidAmount = bidAmount - fee

                    finalAmountSpan.text = "Pago a você: Kz ${
                        String.format("%.2f", bidAmount)
                    } - taxa de Kz ${String.format("%.2f", fee)} = Kz ${
                        String.format("%.2f", paidAmount)
                    }"
                }
            }
        })

        sendButton.setOnClickListener {
            val bidAmount = bidAmountInput.text.toString()
            val deliveryTime = deliveryTimeInput.text.toString()
            val description = descriptionInput.text.toString()

            if (validateInputs(bidAmount, deliveryTime, description)) {
                sendProposal(bidAmount, deliveryTime, description)
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(
        bidAmount: String,
        deliveryTime: String,
        description: String
    ): Boolean {
        return bidAmount.isNotEmpty() && deliveryTime.isNotEmpty() && description.isNotEmpty()
    }

    private fun sendProposal(bidAmount: String, deliveryTime: String, description: String) {
        val userId = firebaseUser.uid
        val proposalId = UUID.randomUUID().toString()

        checkExistingProposal(postId, userId) { hasExistingProposal ->
            if (hasExistingProposal) {
                Toast.makeText(
                    this@ProposalsActivity,
                    "Você já fez uma proposta para este post",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val proposalsMap = HashMap<String, Any>()
                proposalsMap["proposalId"] = proposalId
                proposalsMap["userId"] = userId
                proposalsMap["postId"] = postId
                proposalsMap["projectTitle"] = projectTitle
                proposalsMap["descricao"] = description
                proposalsMap["lance"] = bidAmount
                proposalsMap["numberDays"] = deliveryTime
                proposalsMap["accepted"] = ""
                proposalsMap["rejected"] = ""

                // Consulta ao banco de dados para obter os dados do post
                val postRef = firebaseDatabase.reference.child("Posts").child(postId)
                postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val post = dataSnapshot.getValue(Post::class.java)
                            postOwnerId = post?.userId ?: ""

                            // Verifica se o userId do dono do post é diferente do userId atual
                            if (postOwnerId != userId) {
                                proposalsMap["userIdOther"] = postOwnerId
                            }

                            // Salva a proposta no Firebase
                            val proposalRef = proposalsReference.child(proposalId)
                            proposalRef.setValue(proposalsMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        incrementProposalCount(userId)

                                        loadUserData(userId) { userName, userProfileImage ->
                                            addNotification(postOwnerId, postId, userName, userProfileImage)

                                            // Enviar notificação push
                                            val topic = "/topics/$postOwnerId"
                                            val message = "Nova proposta para o post $projectTitle"
                                            sendPushNotification(topic, message)
                                        }

                                        Toast.makeText(
                                            this@ProposalsActivity,
                                            "Proposta enviada com sucesso",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("Firebase", "Proposta enviada com sucesso")
                                        finish() // Fecha a atividade atual
                                    } else {
                                        Toast.makeText(
                                            this@ProposalsActivity,
                                            "Erro ao enviar a proposta",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            Toast.makeText(
                                this@ProposalsActivity,
                                "Erro: O post não existe",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle onCancelled
                    }
                })
            }
        }
    }

    private fun incrementProposalCount(userId: String) {
        val proposalsStatsRef = firebaseDatabase.reference.child("ProposalStats")
        val userProposalStatsRef = proposalsStatsRef.child(userId)

        userProposalStatsRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val userStats = dataSnapshot.getValue(ProposalsStatistic::class.java)
                userStats?.let {
                    it.proposalsCount += 1
                    userProposalStatsRef.setValue(it)
                }
            } else {
                val newStats = ProposalsStatistic(userId = userId, proposalsCount = 1)
                userProposalStatsRef.setValue(newStats)
                    .addOnSuccessListener {
                        Log.d(
                            "Firebase",
                            "Estatísticas de propostas criadas para o usuário $userId"
                        )
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "Firebase",
                            "Erro ao criar estatísticas de propostas para o usuário $userId",
                            exception
                        )
                    }
            }
        }
    }


    override fun onProposalAccepted(proposal: Proposals) {
        adapter.createManageService(proposal)
        adapter.createManageProject(proposal)
        adapter.createManageServiceHistory(proposal)
    }

    override fun onProposalRejected(proposal: Proposals) {
        //
    }


    private fun checkExistingProposal(
        postId: String,
        userId: String,
        callback: (Boolean) -> Unit
    ) {
        val proposalsQuery = proposalsReference.orderByChild("postId").equalTo(postId)
        proposalsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var hasExistingProposal = false
                for (snapshot in dataSnapshot.children) {
                    val proposal = snapshot.getValue(Proposals::class.java)
                    proposal?.let {
                        if (it.userId == userId) {
                            hasExistingProposal = true

                        }
                    }
                }
                callback(hasExistingProposal)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Erro ao buscar propostas: ${databaseError.message}")
                callback(false) // Chama o callback com false em caso de erro
            }
        })
    }


    private fun addNotification(userId: String, postId: String, userName: String, userProfileImage: String?) {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(userId)
        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = firebaseUser!!.uid
        notiMap["postTitle"] = "Titulo: $projectTitle"
        notiMap["postId"] = postId
        notiMap["ispost"] = true
        notiMap["userName"] = userName
        notiMap["userProfileImage"] = userProfileImage ?: ""

        notiRef.push().setValue(notiMap)
    }


    private fun loadUserData(userId: String, callback: (String, String?) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    val userName = user?.getUsername() ?: ""
                    val userProfileImage = user?.getImage()

                    callback(userName, userProfileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


    private fun sendPushNotification(topic: String, message: String) {
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


}