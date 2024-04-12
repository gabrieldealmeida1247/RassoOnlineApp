package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rassoonlineapp.R

class PortfolioImageAdapter(private val mContext: Context, private val uriArrayList: ArrayList<Uri> = ArrayList()) :
    RecyclerView.Adapter<PortfolioImageAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.portifolio_image_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUri = uriArrayList[position]
        val imagePath = getImagePathFromUri(mContext, imageUri)
        imagePath?.let {
            Glide.with(mContext)
                .load(it)
                .into(holder.imageView)
        }
        // Configurar a remoção da imagem quando o botão for clicado
        holder.removeButton.setOnClickListener {
            removeImage(position)
        }
    }

    override fun getItemCount(): Int {
        return uriArrayList.size
    }

    fun add(uri: Uri) {
        uriArrayList.add(uri)
        notifyDataSetChanged()
    }
    private fun removeImage(position: Int) {
        if (position in 0 until uriArrayList.size) {
            uriArrayList.removeAt(position)
            notifyItemRemoved(position)
            // Atualizar as posições das imagens restantes na lista
            notifyItemRangeChanged(position, uriArrayList.size)
        }
    }

    // Adicione este método para obter todas as URIs das imagens
    fun getAllItems(): List<Uri> {
        return uriArrayList.toList()
    }

    fun setData(uris: List<Uri>) {
        uriArrayList.clear()
        uriArrayList.addAll(uris)
        notifyDataSetChanged()
    }


    fun getImagePathFromUri(context: Context, uri: Uri): String? {
        var imagePath: String? = null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    imagePath = it.getString(columnIndex)
                }
            }
            cursor?.close()
        } else {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if ("com.android.providers.media.documents" == uri.authority) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = MediaStore.Images.Media._ID + "=?"
                    val selectionArgs = arrayOf(split[1])
                    val projection = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor = context.contentResolver.query(contentUri!!, projection, selection, selectionArgs, null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                            imagePath = it.getString(columnIndex)
                        }
                    }
                    cursor?.close()
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = context.contentResolver.query(uri, projection, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                        imagePath = it.getString(columnIndex)
                    }
                }
                cursor?.close()
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                imagePath = uri.path
            }
        }
        return imagePath
    }

    // Adicione este método para adicionar


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val removeButton: TextView = itemView.findViewById(R.id.remove_button)
        val imageView: ImageView = itemView.findViewById(R.id.image)
    }
}
