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
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.example.rassoonlineapp.Model.ProposalsStatistic
import com.example.rassoonlineapp.R
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ProposalsStatisticChartAdapter( private val context: Context, private val proposalsStatistics: List<ProposalsStatistic>) :
    RecyclerView.Adapter<ProposalsStatisticChartAdapter.ProposalsStatisticChartViewHolder>() {

    inner class ProposalsStatisticChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btn_generate_pdf = itemView.findViewById<Button>(R.id.btn_generate_pdf).apply {
            setOnClickListener {
                generateReportPDF(proposalsStatistics)
            }
        }
        val totalProfF: TextView = itemView.findViewById(R.id.total_propF)
        val totalProfR: TextView = itemView.findViewById(R.id.textView_propR)
        val totalProfA: TextView = itemView.findViewById(R.id.textView_propA)
        val totalProfRec: TextView = itemView.findViewById(R.id.textView_propRec)
        val anyChartView: AnyChartView = itemView.findViewById(R.id.pieChartP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProposalsStatisticChartViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.proposals_estatistic_item_layout, parent, false)
        return ProposalsStatisticChartViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProposalsStatisticChartViewHolder, position: Int) {
        val proposalsStatistic = proposalsStatistics[position]

        holder.totalProfF.text = proposalsStatistic.proposalsCount.toString()
        holder.totalProfR.text = proposalsStatistic.proposalsRefuseCount.toString()
        holder.totalProfA.text = proposalsStatistic.proposalsAcceptCount.toString()
        holder.totalProfRec.text = proposalsStatistic.proposalsReceiveCount.toString()

        // Cria o gráfico de pizza usando AnyChart
        val pie = AnyChart.pie()
        val data = listOf(
            ValueDataEntry("Total de Propostas feitas", proposalsStatistic.proposalsCount),
            ValueDataEntry("Total de propostas recebidas", proposalsStatistic.proposalsReceiveCount),
            ValueDataEntry("Total de propostas aceitas", proposalsStatistic.proposalsAcceptCount),
            ValueDataEntry("Total de recusadas", proposalsStatistic.proposalsRefuseCount),

        )
        pie.data(data)
        pie.palette(arrayOf("#ed7d31", "#4472c4", "#a5a5a5", "#FF03DAC5"))
        pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)
        holder.anyChartView.setChart(pie)
    }

    override fun getItemCount(): Int = proposalsStatistics.size

    private fun generateReportPDF(proposalsStatistics: List<ProposalsStatistic>) {
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
            reportData.append("Relatório de Propostas\n\n")

            // Adicione as estatísticas de propostas ao relatório
            reportData.append("Propostas Feitas: ${proposalsStatistics.sumBy { it.proposalsCount }}\n")
            reportData.append("Propostas Recusadas: ${proposalsStatistics.sumBy { it.proposalsRefuseCount }}\n")
            reportData.append("Propostas Recebidas: ${proposalsStatistics.sumBy { it.proposalsReceiveCount }}\n")
            reportData.append("Propostas Aceitas: ${proposalsStatistics.sumBy { it.proposalsAcceptCount }}\n\n")

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
