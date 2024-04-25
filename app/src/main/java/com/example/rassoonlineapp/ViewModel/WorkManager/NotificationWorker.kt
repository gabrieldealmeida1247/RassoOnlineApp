package com.example.rassoonlineapp.ViewModel.WorkManager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rassoonlineapp.Model.PushNotification
import com.example.rassoonlineapp.View.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val notification = inputData.getString("notification")
                val pushNotification = Gson().fromJson(notification, PushNotification::class.java)
                val response = RetrofitInstance.api.postNotification(pushNotification)
                if(response.isSuccessful) {
                    println("WorkManager: Response: ${Gson().toJson(response)}")
                } else {
                    println("WorkManager: Error: ${response.errorBody()?.string()}")
                }
                Result.success()
            } catch(e: Exception) {
                println("WorkManager: Exception: ${e.message}")
                Result.failure()
            }
        }
    }
}
