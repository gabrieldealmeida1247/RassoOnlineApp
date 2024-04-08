package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rassoonlineapp.R

class PortfolioImageAdapter(private val mContext: Context) :
    RecyclerView.Adapter<PortfolioImageAdapter.ViewHolder>() {

    private var uriArrayList: ArrayList<Uri> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.portifolio_image_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUri = uriArrayList[position]
        Glide.with(mContext)
            .load(imageUri)
            .into(holder.imageView)

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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val removeButton: Button = itemView.findViewById(R.id.remove_button)
        val imageView: ImageView = itemView.findViewById(R.id.image)
    }
}
