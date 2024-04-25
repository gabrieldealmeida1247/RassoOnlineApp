package com.example.rassoonlineapp.ViewModel.WorkManager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
class NotificationProposalsWorker (
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId")
        val postId = inputData.getString("postId")
        val projectTitle = inputData.getString("projectTitle")

        if (userId != null && postId != null && projectTitle != null) {
            sendNotification(userId, postId, projectTitle)
        }

        return Result.success()
    }

    private fun sendNotification(userId: String, postId: String, projectTitle: String) {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(userId)
        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = userId
        notiMap["postTitle"] = "Titulo: $projectTitle"
        notiMap["postId"] = postId
        notiMap["ispost"] = true
        notiMap["message"] = "Você recebeu uma nova proposta para o post: $projectTitle"

        notiRef.push().setValue(notiMap)
    }
}