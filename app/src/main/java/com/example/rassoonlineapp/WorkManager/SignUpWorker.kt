package com.example.rassoonlineapp.WorkManager

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignUpWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            Log.d("LoginWorker", "User: ${firebaseUser?.uid ?: "No user logged in"}")

            // Perform background work like data sync or other tasks
            syncUserData(firebaseUser?.uid)

            Result.success()
        } catch (e: Exception) {
            Log.e("LoginWorker", "Error executing background work", e)
            Result.failure()
        }
    }

    private fun syncUserData(userId: String?) {
        userId?.let { uid ->
            val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userData = dataSnapshot.value as? Map<String, Any>

                        userData?.let {
                            val fullName = it["fullname"] as? String ?: ""
                            val userName = it["username"] as? String ?: ""
                            val email = it["email"] as? String ?: ""
                            val description = it["description"] as? String ?: ""
                            val especialidade = it["especialidade"] as? String ?: ""
                            val bio = it["bio"] as? String ?: ""
                            val image = it["image"] as? String ?: ""

                            // Atualize o cache local com os dados do usuário
                            updateLocalCache(fullName, userName, email, description, especialidade, bio, image)

                            // Send broadcast to navigate to SigninActivity
                            val intent = Intent("GO_TO_SIGNIN_ACTIVITY")
                            applicationContext.sendBroadcast(intent)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("LoginWorker", "Error fetching user data: ${databaseError.message}")
                }
            })
        }
    }


    private fun updateLocalCache(
        fullName: String,
        userName: String,
        email: String,
        description: String,
        especialidade: String,
        bio: String,
        image: String
    ) {
        val sharedPreferences = applicationContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("fullName", fullName)
        editor.putString("userName", userName)
        editor.putString("email", email)
        editor.putString("description", description)
        editor.putString("especialidade", especialidade)
        editor.putString("bio", bio)
        editor.putString("image", image)

        editor.apply()
    }
}
