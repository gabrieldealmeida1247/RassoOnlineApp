package com.example.rassoonlineapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Statistic
import com.example.rassoonlineapp.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class ServiceStatisticChartAdapter(private val serviceStatistics: List<Statistic>) :
    RecyclerView.Adapter<ServiceStatisticChartAdapter.ServiceStatisticChartViewHolder>() {

    inner class ServiceStatisticChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val barChart: BarChart = itemView.findViewById(R.id.barChartService)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceStatisticChartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.services_estatistic_item_layout, parent, false)
        return ServiceStatisticChartViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceStatisticChartViewHolder, position: Int) {
        val serviceStatistic = serviceStatistics[position]

        // Preparar os dados para o gráfico
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, serviceStatistic.postsCount.toFloat()))

        val dataSet = BarDataSet(entries, "Posts Count")
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

        holder.barChart.axisLeft.setDrawGridLines(false)
        holder.barChart.axisRight.setDrawGridLines(false)

        holder.barChart.animateY(1000)
        holder.barChart.invalidate()
    }

    override fun getItemCount(): Int = serviceStatistics.size
}
