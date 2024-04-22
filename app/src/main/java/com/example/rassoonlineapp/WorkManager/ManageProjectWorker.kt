package com.example.rassoonlineapp.WorkManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.Model.ManageService
import com.example.rassoonlineapp.Model.Post
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageProjectWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val proposalId = inputData.getString("proposalId")
        val postId = inputData.getString("postId")

        return@withContext try {
            val manageServiceSnapshot = FirebaseDatabase.getInstance().reference.child("ManageService").child(proposalId!!).get().await()
            val manageService = manageServiceSnapshot.getValue(ManageService::class.java)

            val postSnapshot = FirebaseDatabase.getInstance().reference.child("Posts").child(postId!!).get().await()
            val post = postSnapshot.getValue(Post::class.java)

            val databaseReference = FirebaseDatabase.getInstance().reference.child("ManageProject").child(proposalId)
            val manageProjectId = proposalId // Usando proposalId como a chave

            val manageProject = ManageProject(
                manageId = manageProjectId,
                serviceId = "",
                proposalId = proposalId,
                userId = "",
                postId = postId,
                projectName = "",
                description = post?.descricao ?: "",
                skills = post?.habilidades ?: emptyList(),
                workerName = manageService?.workerName ?: "",
                clientName = manageService?.clientName ?: "",
                prazo = "",
                prazoTermino = "",
                pay = manageService?.money ?: "",
                status = "ativo",
                tempoRestante = ""
            )

            databaseReference.setValue(manageProject).await()

            Result.success()

        } catch (e: Exception) {
            Result.failure()
        }
    }
}
