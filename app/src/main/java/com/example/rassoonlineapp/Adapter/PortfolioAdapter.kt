package com.example.rassoonlineapp.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.R

class PortfolioSingleItemAdapter : RecyclerView.Adapter<PortfolioSingleItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Aqui você pode inicializar os elementos do layout, se necessário
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.portfolio_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Aqui você pode definir os dados para os elementos do layout, se necessário
    }

    override fun getItemCount(): Int {
        // Neste caso, estamos exibindo apenas um item
        return 1
    }
}
