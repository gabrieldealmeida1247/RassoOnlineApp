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
import com.example.rassoonlineapp.Model.PaymentStats
import com.example.rassoonlineapp.Model.Statistic
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

class ServicePaymentChartAdapter(private val context: Context, private val payment: List<PaymentStats>) :
    RecyclerView.Adapter<ServicePaymentChartAdapter.ServicePaymentChartViewHolder>() {

    inner class ServicePaymentChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btn_generate_pdf = itemView.findViewById<Button>(R.id.btn_generate_pdfPay).apply {
            setOnClickListener {
                generateReportPDF(payment)
            }
        }

        val totalGain: TextView = itemView.findViewById(R.id.textView_totalGain)
        val totalPay: TextView = itemView.findViewById(R.id.textView_totalPay)
        val totalLastGain: TextView = itemView.findViewById(R.id.textView_lastTotalGain)
        val totalLastPay: TextView = itemView.findViewById(R.id.textView_lastTotalPay)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicePaymentChartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.service_payment_chart_item_layout, parent, false)
        return ServicePaymentChartViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServicePaymentChartViewHolder, position: Int) {
        val payment = payment[position]


        holder.totalGain.text = payment.totalGanho.toString()
        holder.totalPay.text = payment.totalPago.toString()
        holder.totalLastGain.text = payment.lastGainedAmount.toString()
        holder.totalLastPay.text = payment.lastPaidAmount.toString()
        

    }

    override fun getItemCount(): Int = payment.size


    private fun generateReportPDF(payment: List<PaymentStats>) {
        val mDoc = Document()

        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())

        val mFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/" +
                mFileName + ".pdf"

        try {
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))
            mDoc.open()

            // Adicione os dados do relatório ao PDF
            mDoc.add(Paragraph("Relatório de Pagamentos\n\n"))

            // Crie a tabela e adicione cabeçalhos
            val table = PdfPTable(4)
            table.addCell("Total Ganho")
            table.addCell("Total Pago")
            table.addCell("Último Ganho")
            table.addCell("Último Pago")

            // Adicione as estatísticas de pagamento à tabela
            for (paymentStat in payment) {
                table.addCell(paymentStat.totalGanho.toString())
                table.addCell(paymentStat.totalPago.toString())
                table.addCell(paymentStat.lastGainedAmount.toString())
                table.addCell(paymentStat.lastPaidAmount.toString())
            }

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
