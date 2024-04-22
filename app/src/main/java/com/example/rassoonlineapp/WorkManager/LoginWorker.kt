package com.example.rassoonlineapp.WorkManager

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.rassoonlineapp.MainActivity
import com.example.rassoonlineapp.SigninActivity
import com.google.firebase.auth.FirebaseAuth

class LoginWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val mAuth = FirebaseAuth.getInstance()

        try {
            mAuth.currentUser?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Usuário ainda está autenticado
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    applicationContext.startActivity(intent)
                } else {
                    // Usuário não está autenticado, redirecionar para tela de login
                    val intent = Intent(applicationContext, SigninActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    applicationContext.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.e("LoginWorker", "Error checking authentication status", e)
            return Result.failure()
        }

        return Result.success()
    }
}
