package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.R

class PortfolioVideoAdapter(private val mContext: Context) :
    RecyclerView.Adapter<PortfolioVideoAdapter.ViewHolder>() {

    private var uriArrayList: ArrayList<Uri> = ArrayList()

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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val removeButton: Button = itemView.findViewById(R.id.remove_button)
        val videoView: VideoView = itemView.findViewById(R.id.video)
    }
}
