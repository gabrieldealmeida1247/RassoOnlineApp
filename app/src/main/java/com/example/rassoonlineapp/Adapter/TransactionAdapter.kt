package com.example.rassoonlineapp.Adapter

// TransactionAdapter.kt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Transacao
import com.example.rassoonlineapp.R

class TransactionAdapter(private val transactions: List<Transacao>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSender: TextView = itemView.findViewById(R.id.txtSender)
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.txtSender.text = "Remetente: ${transaction.remetente}"
        holder.txtAmount.text = "Quantia: ${transaction.valor}"
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
}
