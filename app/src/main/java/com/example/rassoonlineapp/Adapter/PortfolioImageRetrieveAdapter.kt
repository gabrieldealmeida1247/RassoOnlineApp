package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.R
import com.squareup.picasso.Picasso

class PortfolioImageRetrieveAdapter(private val mContext: Context, private var imageUrlList: List<String>) :
    RecyclerView.Adapter<PortfolioImageRetrieveAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.portfolio_image_retrieve_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get()
            .load(imageUrlList[position])
            .fit()
            .centerCrop()
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            showImageDialog(imageUrlList[position])
        }
    }

    override fun getItemCount(): Int {
        return imageUrlList.size
    }

    fun setData(newImageUrlList: List<String>) {
        imageUrlList = newImageUrlList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_retrieve)
    }

    private fun showImageDialog(imageUrl: String) {
        val builder = AlertDialog.Builder(mContext)
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_image_view, null)
        val dialogImageView = dialogView.findViewById<ImageView>(R.id.dialog_image_view)

        Picasso.get()
            .load(imageUrl)
            .fit()
            .centerInside()
            .into(dialogImageView)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
    }
}
