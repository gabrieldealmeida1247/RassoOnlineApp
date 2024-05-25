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
import com.example.rassoonlineapp.Model.HireStats
import com.example.rassoonlineapp.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ServiceContractChartAdapter(private val context: Context, private val serviceHire: List<HireStats>) :
    RecyclerView.Adapter<ServiceContractChartAdapter.ServiceContractChartViewHolder>() {

    inner class ServiceContractChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val anyChartView: AnyChartView = itemView.findViewById(R.id.pieChartContract)
        val btn_generate_pdf = itemView.findViewById<Button>(R.id.btn_generate_pdfC).apply {
            setOnClickListener {
                generateReportPDF(serviceHire)
            }
        }

        val totalAceite: TextView = itemView.findViewById(R.id.textView_AceiteC)
        val totalRecusado: TextView = itemView.findViewById(R.id.textView_RecusadosC)
        val totalEnviado: TextView = itemView.findViewById(R.id.textView_EnviadaC)

        val totalRecusadoCT: TextView = itemView.findViewById(R.id.textView_RecusadosCT)
        val totalAceiteCT: TextView = itemView.findViewById(R.id.textView_AceiteCT)
        val totalDeletados: TextView = itemView.findViewById(R.id.textView_delete)
        val totalRecebidos: TextView = itemView.findViewById(R.id.textView_receive)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceContractChartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.service_contract_chart_item_layout, parent, false)
        return ServiceContractChartViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceContractChartViewHolder, position: Int) {
        val serviceHire = serviceHire[position]


      holder.totalAceite.text = serviceHire.accepted.toString()
        holder.totalRecusado.text = serviceHire.refused.toString()
        holder.totalEnviado.text = serviceHire.totalHires.toString()

        holder.totalAceiteCT.text = serviceHire.acceptedC.toString()
        holder.totalRecusadoCT.text = serviceHire.refusedC.toString()

        holder.totalDeletados.text = serviceHire.totalHiresDelete.toString()
        holder.totalRecebidos.text = serviceHire.totalHiresReceive.toString()



// Cria o gráfico de pizza usando AnyChart
        val pie = AnyChart.pie()
        val data = listOf(
            ValueDataEntry("Total de Contratações enviada", serviceHire.totalHires),
            ValueDataEntry("Total de Contratações aceite", serviceHire.accepted),
            ValueDataEntry("Total de  Contratações recusada", serviceHire.refused),
            ValueDataEntry("Total de Contratações aceite pelo usuário atual",serviceHire.acceptedC),
            ValueDataEntry("Total de Contratações recusado pelo usuário atual",serviceHire.refusedC),
            ValueDataEntry("Total de Contratações deletado", serviceHire.totalHiresDelete),
            ValueDataEntry("Total de Contratações recebido", serviceHire.totalHiresReceive),

            )
        pie.data(data)
        pie.palette(arrayOf("#e27a3f","#FF03DAC5", "#FF3700B3", "#ff68a5", "#5c32c7", "#FF018786", "#FB0000"))
        pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)
        holder.anyChartView.setChart(pie)
    }

    override fun getItemCount(): Int = serviceHire.size

    private fun generateReportPDF(serviceHire: List<HireStats>) {
        val mDoc = Document()

        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())

        val mFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/" +
                mFileName + ".pdf"

        try {
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))
            mDoc.open()

            // Adicione os dados do relatório ao PDF
            val reportData = Paragraph("Relatório de Serviços\n\n")
            mDoc.add(reportData)

            // Crie uma tabela para os dados
            val table = PdfPTable(2) // 2 colunas

            // Adicione os cabeçalhos da tabela
            table.addCell("Descrição")
            table.addCell("Valor")

            // Adicione os dados das estatísticas de serviços
            table.addCell("Serviços contratados aceitos")
            table.addCell(serviceHire.sumBy { it.accepted }.toString())
            table.addCell("Serviços contratados recusados")
            table.addCell(serviceHire.sumBy { it.refused }.toString())
            table.addCell("Serviços contratados pelo usuário atual aceitos")
            table.addCell(serviceHire.sumBy { it.acceptedC }.toString())
            table.addCell("Serviços contratados pelo usuário atual recusados")
            table.addCell(serviceHire.sumBy { it.refusedC }.toString())
            table.addCell("Total de serviços contratados deletados")
            table.addCell(serviceHire.sumBy { it.totalHiresDelete }.toString())
            table.addCell("Total de serviços contratados recebidos")
            table.addCell(serviceHire.sumBy { it.totalHiresReceive }.toString())
            table.addCell("Total de Contratações enviadas")
            table.addCell(serviceHire.sumBy { it.totalHires }.toString())

            // Adicione a tabela ao documento
            mDoc.add(table)
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
