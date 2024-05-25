package com.example.rassoonlineapp.Admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.example.rassoonlineapp.Admin.model.ServiceCount
import com.example.rassoonlineapp.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminServiceAdapter : RecyclerView.Adapter<AdminServiceAdapter.AdminServiceViewHolder>() {

    private lateinit var serviceCount: ServiceCount
    private var databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("ServiceCount")

    inner class AdminServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val  textView_totEli: TextView = itemView.findViewById(R.id. textView_totEli)
        val textView_totPub: TextView = itemView.findViewById(R.id.textView_totPub)
        val  textView_totProp: TextView = itemView.findViewById(R.id. textView_totProp)
        val   textView_totRefuse: TextView = itemView.findViewById(R.id.textView_totRefuse)
        val   textView_totAccept: TextView = itemView.findViewById(R.id.textView_totAccept)
        val     textView_totConclude: TextView = itemView.findViewById(R.id.   textView_totConclude)
        val     textView_totCancel: TextView = itemView.findViewById(R.id.   textView_totCancel)
        val anyChartView: AnyChartView = itemView.findViewById(R.id.pieChart)
        val lineChartView: LineChart = itemView.findViewById(R.id.lineChart)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminServiceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_service_item_layout, parent, false)
        return AdminServiceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AdminServiceViewHolder, position: Int) {
            holder.textView_totPub.text = serviceCount.postsCount.toString()
            holder.textView_totProp.text = serviceCount.propCount.toString()
        holder.textView_totAccept.text = serviceCount.proposalsAcceptCount.toString()
        holder.textView_totRefuse.text = serviceCount.proposalsRefuseCount.toString()
        holder.textView_totConclude.text = serviceCount.concludeCount.toString()
        holder.textView_totCancel.text = serviceCount.cancelCount.toString()
        holder.textView_totEli.text = serviceCount.deleteCount.toString()

        // Cria o gráfico de pizza usando AnyChart
        val pie = AnyChart.pie()
        val data = listOf(
            ValueDataEntry("Total de serviços publicados", serviceCount.postsCount),
            ValueDataEntry("Total de propostas", serviceCount.propCount),
            ValueDataEntry("Total de serviços aceite", serviceCount.proposalsAcceptCount),
            ValueDataEntry("Total de serviços recusada", serviceCount.proposalsRefuseCount),
            ValueDataEntry("Total de serviços concluido", serviceCount.concludeCount),
            ValueDataEntry("Total de serviços cancelado", serviceCount.cancelCount) ,
            ValueDataEntry("Total de serviços deletado", serviceCount.deleteCount)
        )
        pie.data(data)
        pie.palette(arrayOf("#ed7d31", "#4472c4", "#a5a5a5", "#FF03DAC5", "#FB0000","#FF6200EE","#FF000000"))
        pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)
        holder.anyChartView.setChart(pie)

        // Configura o LineChart com dados de aumento mensal e redução mensal
        val increaseData = getMonthlyIncreaseData()
        val decreaseData = getMonthlyDecreaseData()
        val increaseEntries = mutableListOf<Entry>()
        val decreaseEntries = mutableListOf<Entry>()

        for (i in increaseData.indices) {
            increaseEntries.add(Entry(i.toFloat(), increaseData[i]))
        }
        for (i in decreaseData.indices) {
            decreaseEntries.add(Entry(i.toFloat(), decreaseData[i]))
        }

        val increaseDataSet = LineDataSet(increaseEntries, "Aumento Mensal")
        increaseDataSet.color = ColorTemplate.COLORFUL_COLORS[0]
        increaseDataSet.setDrawValues(true)
        increaseDataSet.setDrawFilled(true)

        val decreaseDataSet = LineDataSet(decreaseEntries, "Redução Mensal")
        decreaseDataSet.color = ColorTemplate.COLORFUL_COLORS[1]
        decreaseDataSet.setDrawValues(true)
        decreaseDataSet.setDrawFilled(true)

        val lineData = LineData(increaseDataSet, decreaseDataSet)
        holder.lineChartView.data = lineData

        val xAxis = holder.lineChartView.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(getMonths())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -45f

        holder.lineChartView.invalidate()

    }

    override fun getItemCount(): Int {
        // Aqui você retorna o tamanho da lista de dados
        // por exemplo: return dataList.size
        return 1 // Por enquanto, retorna 0, pois não há dados
    }

    init {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serviceCount = snapshot.getValue(ServiceCount::class.java) ?: ServiceCount(0,0,0,0,0,0,0)
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }


    private fun getMonthlyIncreaseData(): List<Float> {
        // Substitua isso pelos seus cálculos reais de aumento mensal
        return listOf(5f, 10f, 15f, 20f, 25f, 30f, 35f, 40f, 45f, 50f, 55f, 60f)
    }

    private fun getMonthlyDecreaseData(): List<Float> {
        // Substitua isso pelos seus cálculos reais de redução mensal
        return listOf(3f, 6f, 9f, 12f, 15f, 18f, 21f, 24f, 27f, 30f, 33f, 36f)
    }

    private fun getMonths(): List<String> {
        // Retorna apenas o mês atual do sistema
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
        return listOf(dateFormat.format(calendar.time))
    }
}
