package com.example.rassoonlineapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.ProposalsStatistic
import com.example.rassoonlineapp.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ProposalsStatisticChartAdapter(private val proposalsStatistics: List<ProposalsStatistic>) :
    RecyclerView.Adapter<ProposalsStatisticChartAdapter.ProposalsStatisticChartViewHolder>() {

    inner class ProposalsStatisticChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val barChart: BarChart = itemView.findViewById(R.id.barChartService)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProposalsStatisticChartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.proposals_estatistic_item_layout, parent, false)
        return ProposalsStatisticChartViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProposalsStatisticChartViewHolder, position: Int) {
        val proposalsStatistic = proposalsStatistics[position]

        // Certifique-se de que o valor de "Propostas Recusadas" não seja zero
        val refuseCount = if (proposalsStatistic.proposalsRefuseCount > 0) proposalsStatistic.proposalsRefuseCount.toFloat() else 1f

        // Preparar os dados para o gráfico
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, proposalsStatistic.proposalsCount.toFloat()))
        entries.add(BarEntry(1f, refuseCount))  // Usando o valor ajustado
        entries.add(BarEntry(2f, proposalsStatistic.proposalsReceiveCount.toFloat()))
        entries.add(BarEntry(3f, proposalsStatistic.proposalsAcceptCount.toFloat()))

        val labels = listOf("feitas", "Recusadas", "Recebida", "Aceite")

        val dataSet = BarDataSet(entries, "Proposals Statistics")

        // Definir cores para as barras
        dataSet.colors = listOf(
            holder.itemView.resources.getColor(R.color.colorPrimary),
            holder.itemView.resources.getColor(R.color.red),  // Cor para "Recusadas"
            holder.itemView.resources.getColor(R.color.green),
            holder.itemView.resources.getColor(R.color.colorBlue)
        )

        val data = BarData(dataSet)

        // Configurar o gráfico
        holder.barChart.data = data
        holder.barChart.setFitBars(true)
        holder.barChart.description.isEnabled = false
        holder.barChart.setDrawValueAboveBar(true)
        holder.barChart.setDrawGridBackground(false)

        // Configurar eixos
        val xAxis = holder.barChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        holder.barChart.axisLeft.setDrawGridLines(false)
        holder.barChart.axisRight.setDrawGridLines(false)

        holder.barChart.animateY(1000)
        holder.barChart.invalidate()
    }

    override fun getItemCount(): Int = proposalsStatistics.size
}
