package com.example.rassoonlineapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rassoonlineapp.Adapter.PortfolioImageAdapter
import com.example.rassoonlineapp.Adapter.PortfolioVideoAdapter
import com.example.rassoonlineapp.Model.PortfolioItem
import com.example.rassoonlineapp.WorkManager.PortfolioWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PortfolioActivity : AppCompatActivity() {

    private lateinit var portfolioImageAdapter: PortfolioImageAdapter
    private lateinit var portfolioVideoAdapter: PortfolioVideoAdapter
    private lateinit var recyclerViewImage: RecyclerView
    private lateinit var recyclerViewVideo: RecyclerView
    private lateinit var uploadButtonImage: Button
    private lateinit var uploadButtonVideo: Button
    private lateinit var totalPhotos: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio)

        totalPhotos = findViewById(R.id.total_photos)

        recyclerViewImage = findViewById(R.id.recycler_view_port_image)
        recyclerViewVideo = findViewById(R.id.recycler_view_port_video)

        uploadButtonImage = findViewById(R.id.upload_button_image)
        uploadButtonVideo = findViewById(R.id.upload_button_video)

        recyclerViewImage.layoutManager = GridLayoutManager(this, 3)
        recyclerViewVideo.layoutManager = GridLayoutManager(this, 3)

        portfolioImageAdapter = PortfolioImageAdapter(this)
        portfolioVideoAdapter = PortfolioVideoAdapter(this)

        recyclerViewImage.adapter = portfolioImageAdapter
        recyclerViewVideo.adapter = portfolioVideoAdapter

        findViewById<Button>(R.id.send_portfolio_button).setOnClickListener {
            savePortfolio()
            onBackPressed()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                Read_Permission
            )
        }

        uploadButtonImage.setOnClickListener {
            openGalleryForImages()
        }

        uploadButtonVideo.setOnClickListener {
            openGalleryForVideo()
        }

        // Inicie o WorkManager
        startPortfolioWork()
    }


    private fun startPortfolioWork() {
        val workRequest = OneTimeWorkRequestBuilder<PortfolioWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun openGalleryForImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                REQUEST_CODE_IMAGE
            )
        } else {
            Toast.makeText(this, "No apps can perform this action", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGalleryForVideo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "video/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_CODE_VIDEO)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                val clipData = data.clipData
                clipData?.let {
                    val itemCount = it.itemCount
                    for (i in 0 until itemCount) {
                        val imageUri: Uri = it.getItemAt(i).uri
                        portfolioImageAdapter.add(imageUri)
                    }
                    portfolioImageAdapter.notifyDataSetChanged()
                    totalPhotos.text = "Selected: ${portfolioImageAdapter.itemCount}"
                }
            } else if (data.data != null) {
                val imageUri: Uri = data.data!!
                portfolioImageAdapter.add(imageUri)
                portfolioImageAdapter.notifyDataSetChanged()
                totalPhotos.text = "Selected: ${portfolioImageAdapter.itemCount}"
            }
        } else if (requestCode == REQUEST_CODE_VIDEO && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                val clipData = data.clipData
                clipData?.let {
                    val itemCount = it.itemCount
                    for (i in 0 until itemCount) {
                        val videoUri: Uri = it.getItemAt(i).uri
                        grantUriPermission(
                            packageName,
                            videoUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        portfolioVideoAdapter.add(videoUri)
                    }
                    portfolioVideoAdapter.notifyDataSetChanged()
                }
            } else if (data.data != null) {
                val videoUri: Uri = data.data!!
                grantUriPermission(
                    packageName,
                    videoUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                portfolioVideoAdapter.add(videoUri)
                portfolioVideoAdapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        const val Read_Permission: Int = 101
        const val REQUEST_CODE_IMAGE: Int = 1
        const val REQUEST_CODE_VIDEO: Int = 2
    }

    private fun sendPortfolioItem(portfolioId: String, portfolioItem: PortfolioItem) {
        // Obtendo uma referência para o nó "portfolio" no banco de dados Firebase
        val databaseReference = FirebaseDatabase.getInstance().reference.child("portfolio")

        // Usando o portfolioId como chave para o item do portfólio
        val portfolioRef = databaseReference.child(portfolioId)

        // Definindo os valores do portfólio no banco de dados
        portfolioRef.setValue(portfolioItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Portfolio item enviado com sucesso!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao enviar o item do portfólio: $it", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun savePortfolio() {
        lifecycleScope.launch {
            val portfolioId =
                FirebaseDatabase.getInstance().reference.child("portfolio").push().key ?: ""
            val images = portfolioImageAdapter.getAllItems()
            val videos = portfolioVideoAdapter.getAllItems()
            val profileId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val storage = FirebaseStorage.getInstance().reference

            // Verifica se há pelo menos uma imagem ou um vídeo selecionado
            if (images.isNotEmpty() || videos.isNotEmpty()) {
                val imageUrls = mutableListOf<String>()
                val videoUrls = mutableListOf<String>()

                // Salvar imagens
                images.forEachIndexed { index, uri ->
                    val imageName = "image_$portfolioId$index.jpg"
                    val imageRef =
                        storage.child("portfolio_images").child(profileId).child(imageName)
                    val uploadTask = imageRef.putFile(uri)

                    try {
                        uploadTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            imageRef.downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUri = task.result
                                imageUrls.add(downloadUri.toString())

                                if (imageUrls.size == images.size) {
                                    // Salvar vídeos após salvar todas as imagens
                                    videos.forEachIndexed { index, videoUri ->
                                        val videoName = "video_$portfolioId$index.mp4"
                                        val videoRef =
                                            storage.child("portfolio_videos").child(profileId)
                                                .child(videoName)
                                        val videoUploadTask = videoRef.putFile(videoUri)

                                        videoUploadTask.continueWithTask { videoTask ->
                                            if (!videoTask.isSuccessful) {
                                                videoTask.exception?.let {
                                                    throw it
                                                }
                                            }
                                            videoRef.downloadUrl
                                        }.addOnCompleteListener { videoTask ->
                                            if (videoTask.isSuccessful) {
                                                val videoDownloadUri = videoTask.result
                                                videoUrls.add(videoDownloadUri.toString())

                                                if (videoUrls.size == videos.size) {
                                                    // Todos os uploads foram concluídos, agora envie para o Realtime Database
                                                    val portfolioItem = PortfolioItem(
                                                        portfolioId,
                                                        imageUrls,
                                                        videoUrls
                                                    )
                                                    sendPortfolioItem(portfolioId, portfolioItem)
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this@PortfolioActivity,
                                                    "Erro ao enviar vídeo: ${videoTask.exception}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    this@PortfolioActivity,
                                    "Erro ao enviar imagem: ${task.exception}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@PortfolioActivity,
                            "Erro ao fazer upload: $e",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@PortfolioActivity,
                    "Selecione pelo menos uma imagem ou vídeo para enviar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }

}

