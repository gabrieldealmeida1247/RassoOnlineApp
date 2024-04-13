package com.example.rassoonlineapp.Adapter

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.R
import com.squareup.picasso.Picasso

class VideoRetrieveAdapter(private val mContext: Context, private var videoUrlList: List<String>) :
    RecyclerView.Adapter<VideoRetrieveAdapter.ViewHolder>() {

    private val videoViews: MutableList<VideoView> = mutableListOf()
    private var currentVideoView: VideoView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.video_retrieve_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Load a placeholder image for the video
        Picasso.get()
            .load(R.drawable.video_placeholder) // You can use a placeholder image or a thumbnail here
            .fit()
            .centerCrop()
            .into(holder.videoThumbnail)
/*
        holder.videoThumbnail.setOnClickListener {
            // Ocultar miniatura e exibir VideoView
            holder.videoThumbnail.visibility = View.GONE
            holder.videoView.visibility = View.VISIBLE

            // Setar a URL do vídeo no VideoView
            val videoUri = Uri.parse(videoUrlList[position])
            holder.videoView.setVideoURI(videoUri)

            // Iniciar reprodução
            holder.videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
                mp.start()

            }

            // Parar a reprodução de vídeo anterior, se houver
            currentVideoView?.let { videoView ->
                if (videoView.isPlaying) {
                    videoView.stopPlayback()
                    videoView.visibility = View.GONE
                    videoView.seekTo(0)
                }
            }

            // Atualizar o VideoView atualmente em reprodução
            currentVideoView = holder.videoView

            val intent = Intent(mContext, FullScreenVideoActivity::class.java)
            intent.putExtra("videoUrl", videoUrlList[position])
            mContext.startActivity(intent)
        }



 */
/*
        holder.videoThumbnail.setOnClickListener {
            val intent = Intent(mContext, FullScreenVideoActivity::class.java)
            intent.putExtra("videoUrl", videoUrlList[position])
            mContext.startActivity(intent)
        }


 */

        holder.videoThumbnail.setOnClickListener {
            showVideoDialog(videoUrlList[position])
        }

    }

    override fun getItemCount(): Int {
        return videoUrlList.size
    }

    fun setData(newVideoUrlList: List<String>) {
        videoUrlList = newVideoUrlList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoThumbnail: ImageView = itemView.findViewById(R.id.video_thumbnail)
        val videoView: VideoView = itemView.findViewById(R.id.video_view)

        init {
            videoViews.add(videoView)

        }


    }

    private fun showVideoDialog(videoUrl: String) {
        val dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_video_player)

        val videoView = dialog.findViewById<VideoView>(R.id.video_view_dialog)

        val customMediaController = CustomMediaController(mContext)
        customMediaController.setAnchorView(videoView)

        videoView.setMediaController(customMediaController)

        val videoUri = Uri.parse(videoUrl)
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.start()
        }

        val btnPlay = dialog.findViewById<ImageButton>(R.id.btn_play)
        val btnStop = dialog.findViewById<ImageButton>(R.id.btn_stop)
        val btnForward = dialog.findViewById<ImageButton>(R.id.btn_forward)

        btnPlay.setOnClickListener {
            if (!videoView.isPlaying) {
                videoView.start()
            }
        }

        btnStop.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
            }
        }

        videoView.setOnCompletionListener {
            customMediaController.hide()
        }


        dialog.setOnDismissListener {
            customMediaController.hide()
        }


        btnForward.setOnClickListener {
            val currentPosition = videoView.currentPosition
            val forwardTime = 10000 // avançar 10 segundos
            if (currentPosition + forwardTime <= videoView.duration) {
                videoView.seekTo(currentPosition + forwardTime)
            }
        }

        dialog.show()
    }


}
