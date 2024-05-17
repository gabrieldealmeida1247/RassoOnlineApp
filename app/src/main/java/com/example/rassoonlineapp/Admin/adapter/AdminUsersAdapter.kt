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
import com.example.rassoonlineapp.Admin.model.UserCount
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

class AdminUsersAdapter : RecyclerView.Adapter<AdminUsersAdapter.AdminUsersViewHolder>() {

    private lateinit var userCount: UserCount
    private var databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("UserCount")

    inner class AdminUsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewUserCount: TextView = itemView.findViewById(R.id.total_de_usuarios)
        val textView_qtd_delete: TextView = itemView.findViewById(R.id.textView_qtd_delete)
        val textView_qtd_login: TextView = itemView.findViewById(R.id.textView_qtd_login)
        val textView_qtd_deslocado: TextView = itemView.findViewById(R.id.textView_qtd_deslocado)
        val anyChartView: AnyChartView = itemView.findViewById(R.id.pieChart)
        val lineChartView: LineChart = itemView.findViewById(R.id.lineChart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminUsersViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_users_item_layout, parent, false)
        return AdminUsersViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AdminUsersViewHolder, position: Int) {
        holder.textViewUserCount.text = userCount.count.toString()
        holder.textView_qtd_delete.text = userCount.deleteCount.toString()
        holder.textView_qtd_login.text = userCount.loggedInCount.toString()
        holder.textView_qtd_deslocado.text = userCount.loggedOutCount.toString()

        // Cria o gráfico de pizza usando AnyChart
        val pie = AnyChart.pie()
        val data = listOf(
            ValueDataEntry("Total de usuários", userCount.count),
            ValueDataEntry("Total de deletados", userCount.deleteCount),
            ValueDataEntry("Total de usuários banidos", userCount.bannedUserCount),
            ValueDataEntry("Total de usuários Logados", userCount.loggedInCount),
            ValueDataEntry("Total de usuários deslogado", userCount.loggedOutCount)
        )
        pie.data(data)
        pie.palette(arrayOf("#ed7d31", "#4472c4", "#a5a5a5", "#FF03DAC5", "#FB0000"))
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
        return 1
    }

    init {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userCount = snapshot.getValue(UserCount::class.java) ?: UserCount(0, 0, 0, 0, 0)
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