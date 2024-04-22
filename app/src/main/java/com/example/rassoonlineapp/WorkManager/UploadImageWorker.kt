package com.example.rassoonlineapp.WorkManager

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadImageWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val imageUri = inputData.getString("imageUri")
            val userId = inputData.getString("userId")

            if (!imageUri.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                val storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Picture")
                val fileRef = storageProfilePicRef.child("$userId.jpg")
                val uploadTask = fileRef.putFile(imageUri.toUri())

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@continueWithTask fileRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        val myUrl = downloadUrl.toString()

                        // Atualizar no Realtime Database
                        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                        val userMap = HashMap<String, Any>()
                        userMap["image"] = myUrl

                        usersRef.updateChildren(userMap)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
