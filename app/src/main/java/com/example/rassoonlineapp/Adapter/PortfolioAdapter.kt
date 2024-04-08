package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.PortfolioActivity
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseUser

class PortfolioAdapter(private val mContext: Context,) :
    RecyclerView.Adapter<PortfolioAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.portfolio_item_layout, parent, false)
        val manageButton = view.findViewById<Button>(R.id.manageButton)
        manageButton.setOnClickListener {
            val intent = Intent(mContext, PortfolioActivity::class.java)
            mContext.startActivity(intent)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


    }

    override fun getItemCount(): Int {
      return 1
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }


}