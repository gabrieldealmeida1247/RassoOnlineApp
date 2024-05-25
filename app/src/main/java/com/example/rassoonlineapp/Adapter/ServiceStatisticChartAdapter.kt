package com.example.rassoonlineapp.Adapter

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Model.Statistic
import com.example.rassoonlineapp.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ServiceStatisticChartAdapter(private val context: Context, private val serviceStatistics: List<Statistic>) :
    RecyclerView.Adapter<ServiceStatisticChartAdapter.ServiceStatisticChartViewHolder>() {

    inner class ServiceStatisticChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val barChart: BarChart = itemView.findViewById(R.id.barChartService)
        val btn_generate_pdf = itemView.findViewById<Button>(R.id.btn_generate_pdf).apply {
            setOnClickListener {
                generateReportPDF(serviceStatistics)
            }
        }

        val totalServices: TextView = itemView.findViewById(R.id.total_de_post)
        val totalConclude: TextView = itemView.findViewById(R.id.textView_conclude)
        val totalCancel: TextView = itemView.findViewById(R.id.textView_cancelados)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceStatisticChartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.services_estatistic_item_layout, parent, false)
        return ServiceStatisticChartViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceStatisticChartViewHolder, position: Int) {
        val serviceStatistic = serviceStatistics[position]


        holder.totalServices.text = serviceStatistic.postsCount.toString()
        holder.totalCancel.text = serviceStatistic.serviceCancel.toString()
        holder.totalConclude.text = serviceStatistic.serviceConclude.toString()
        
        // Preparar os dados para o gráfico
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, serviceStatistic.postsCount.toFloat()))
        entries.add(BarEntry(1f, serviceStatistic.serviceConclude.toFloat()))
        entries.add(BarEntry(2f, serviceStatistic.serviceCancel.toFloat()))

        val labels = listOf("Posts Count", "Service Conclude", "Service Cancel")

        val dataSet = BarDataSet(entries, "Service Statistics")
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

    override fun getItemCount(): Int = serviceStatistics.size

    private fun generateReportPDF(serviceStatistics: List<Statistic>) {
        val mDoc = Document()

        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())

        val mFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/" +
                mFileName + ".pdf"

        try {
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))
            mDoc.open()

            // Adicione os dados do relatório ao PDF
            val reportData = StringBuilder()
            reportData.append("Relatório de Serviços\n\n")

            // Adicione as estatísticas de serviços ao relatório
            reportData.append("Posts Count: ${serviceStatistics.sumBy { it.postsCount }}\n")
            reportData.append("Service Conclude: ${serviceStatistics.sumBy { it.serviceConclude }}\n")
            reportData.append("Service Cancel: ${serviceStatistics.sumBy { it.serviceCancel }}\n\n")

            // Adicione os dados do relatório ao PDF
            mDoc.add(Paragraph(reportData.toString()))
            mDoc.close()

            // Exibir notificação
            showNotification(mFileName, mFilePath)
        } catch (e: Exception) {
            Log.e("PDF", "Erro ao criar PDF", e)
        }
    }

    private fun showNotification(fileName: String, filePath: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel for devices with API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PDF Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open PDF file
        val openPdfIntent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(filePath)
        )
        openPdfIntent.setDataAndType(fileUri, "application/pdf")
        openPdfIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openPdfIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_rasso)
            .setContentTitle("PDF Criado")
            .setContentText("$fileName.pdf foi criado em $filePath")
            .setContentIntent(pendingIntent) // Set PendingIntent
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(1, builder.build())
        }
    }

    companion object {
        private const val CHANNEL_ID = "PDF_NOTIFICATION_CHANNEL"
    }

}
