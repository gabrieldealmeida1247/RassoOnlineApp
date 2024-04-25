package com.example.rassoonlineapp.ViewModel.WorkManager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class PortfolioWorker (appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            // Implemente sua lógica de upload aqui
            // Por exemplo: Simular uma operação de upload de arquivo
            Thread.sleep(5000) // Delay de 5 segundos
            Result.success()
        }
    }
}