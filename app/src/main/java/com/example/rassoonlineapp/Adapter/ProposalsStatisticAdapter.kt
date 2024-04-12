package com.example.rassoonlineapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ProposalsStatistic
import com.example.rassoonlineapp.R

class ProposalsStatisticAdapter(
    private val context: Context, private val statisticProposals: List<ProposalsStatistic>
) :
    RecyclerView.Adapter<ProposalsStatisticAdapter.ServiceStatisticViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceStatisticViewHolder {
        val itemView = LayoutInflater.from(context).inflate(
            R.layout.proposals_statistic_item_layout, parent, false)
        return ServiceStatisticViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceStatisticViewHolder, position: Int) {
        val currentUser = statisticProposals[position]
        holder.textViewRefuse.text = "Propostas Rececusadas: ${currentUser.proposalsRefuseCount}"
        holder.textViewReceive.text = "Propostas Recebidas: ${currentUser.proposalsReceiveCount}"
        holder.textViewAccept.text = "Propostas Aceitas: ${currentUser.proposalsAcceptCount}"
        holder.textViewMaked.text = "Propostas Feitas: ${currentUser.proposalsCount}"


    }

    override fun getItemCount() = statisticProposals.size

    inner class ServiceStatisticViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewReceive: TextView = itemView.findViewById(R.id.textView_qtd_proposals_receive)
        val textViewAccept: TextView = itemView.findViewById(R.id.textView_qtd_proposals_accept)
        val textViewRefuse: TextView = itemView.findViewById(R.id.textView_qtd_proposals_refused)
        val textViewMaked: TextView = itemView.findViewById(R.id.textView_qtd_proposals_maked)
    }
}
