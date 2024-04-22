package com.example.rassoonlineapp.WorkManager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class RefreshDataWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Código para atualizar os dados em segundo plano
        return Result.success()
    }
}