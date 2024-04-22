package com.example.rassoonlineapp.WorkManager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rassoonlineapp.Adapter.ChatAdapter
import com.example.rassoonlineapp.Model.Chat
import com.example.rassoonlineapp.Model.Proposals
import com.example.rassoonlineapp.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class FetchUsersWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@coroutineScope Result.failure()

        val proposalsRef = FirebaseDatabase.getInstance().getReference("Proposals")
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        val chatList = ArrayList<Chat>()
        val userIdList = HashSet<String>()

        // Fetch users who made proposals to the current user
        val proposalsDeferred = async {
            fetchUsersFromProposals(proposalsRef, userId, "userIdOther")
        }

        // Fetch users who accepted proposals from the current user
        val acceptedDeferred = async {
            fetchUsersFromProposals(proposalsRef, userId, "userId")
        }

        val userIdOthersList = proposalsDeferred.await()
        val userIdAcceptedList = acceptedDeferred.await()

        userIdList.addAll(userIdOthersList)
        userIdList.addAll(userIdAcceptedList)

        val usersSnapshot = usersRef.get().await()

        for (userId in userIdList) {
            val userSnapshot = usersSnapshot.child(userId)
            val user = userSnapshot.getValue(User::class.java)
            user?.let {
                val chat = Chat(it.getUsername(), it.getImage(), userId)
                chatList.add(chat)
            }
        }

        updateRecyclerViewAdapter(chatList)
        Result.success()
    }

    private suspend fun fetchUsersFromProposals(
        proposalsRef: DatabaseReference,
        userId: String,
        childKey: String
    ): List<String> = coroutineScope {
        val userIdList = ArrayList<String>()

        proposalsRef.orderByChild(childKey).equalTo(userId).get().await().children.forEach { dataSnapshot ->
            val proposal = dataSnapshot.getValue(Proposals::class.java)
            proposal?.let {
                if (proposal.accepted == "Aprovado") {
                    val userIdOther = if (childKey == "userIdOther") proposal.userId else proposal.userIdOther
                    userIdOther?.let { userIdList.add(it) }
                }
            }
        }
        userIdList
    }

    private fun updateRecyclerViewAdapter(chatList: ArrayList<Chat>) {
        val chatAdapter = ChatAdapter(applicationContext, chatList)
        // Assuming you have a way to access the RecyclerView from here
        // e.g., through a callback or ViewModel
        // recyclerView.adapter = chatAdapter
    }
}