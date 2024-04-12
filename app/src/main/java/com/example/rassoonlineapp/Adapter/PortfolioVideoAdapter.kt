package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.R

class PortfolioVideoAdapter(private val mContext: Context, private val uriArrayList: ArrayList<Uri> = ArrayList()) :
    RecyclerView.Adapter<PortfolioVideoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.portifolio_video_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videoUri = uriArrayList[position]
        holder.videoView.setVideoURI(videoUri)

        // Configurar a remoção do vídeo quando o botão for clicado
        holder.removeButton.setOnClickListener {
            removeVideo(position)
        }
    }

    override fun getItemCount(): Int {
        return uriArrayList.size
    }

    fun add(uri: Uri) {
        uriArrayList.add(uri)
        notifyDataSetChanged()
    }

    private fun removeVideo(position: Int) {
        if (position in 0 until uriArrayList.size) {
            uriArrayList.removeAt(position)
            notifyItemRemoved(position)
            // Atualizar as posições dos vídeos restantes na lista
            notifyItemRangeChanged(position, uriArrayList.size)
        }
    }
    fun getAllItems(): List<Uri> {
        return uriArrayList.toList()
    }

    // Adicione este método para adicionar todos os itens de uma lista de URIs
    fun addAll(list: List<String>) {
        // Limpar a lista atual antes de adicionar novos itens
        uriArrayList.clear()

        // Converter cada String em Uri e adicionar à lista
        for (uriString in list) {
            val uri = Uri.parse(uriString)
            uriArrayList.add(uri)
        }

        notifyDataSetChanged()
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val removeButton: TextView = itemView.findViewById(R.id.remove_button)
        val videoView: VideoView = itemView.findViewById(R.id.video)
    }
}
