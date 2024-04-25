package com.example.rassoonlineapp.ViewModel.WorkManager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class ManageWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            // Implemente sua lógica de trabalho aqui
            // Por exemplo: Simular uma operação de longa duração
            Thread.sleep(5000) // Delay de 5 segundos
            Result.success()
        }
    }
}