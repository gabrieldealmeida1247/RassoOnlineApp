package com.example.rassoonlineapp.Fragments

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.rassoonlineapp.R
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class FinancialReportsFragment : Fragment() {

private lateinit var et_pdf_data: EditText
private lateinit var btn_generate_pdf: Button
private val STORAGE_CODE = 1001
    private val CHANNEL_ID = "PDF_NOTIFICATION_CHANNEL"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_financial_reports, container, false)

        et_pdf_data = view.findViewById(R.id.et_pdf_data)
        btn_generate_pdf = view.findViewById(R.id.btn_generate_pdf)

        btn_generate_pdf.setOnClickListener{
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){

                    val permission = arrayListOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission.toTypedArray(), STORAGE_CODE)


                }else{
                    savePDF()
                }
            }else{
                savePDF()
            }
        }


        return view
    }

    private fun savePDF() {
        val mDoc = Document()

        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())

        val mFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/" +
                mFileName + ".pdf"

        try {
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))
            mDoc.open()

            val data = et_pdf_data.text.toString().trim()
            mDoc.addAuthor("KB CODER")
            mDoc.add(Paragraph(data))
            mDoc.close()

            // Display notification
            showNotification(mFileName, mFilePath)
            Toast.makeText(requireContext(), "$mFileName.pdf\n foi criado em \n$mFilePath", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erro ao criar PDF: ${e.toString()}", Toast.LENGTH_SHORT).show()
            Log.e("PDF", "Erro ao criar PDF", e)
        }
    }


    private fun showNotification(fileName: String, filePath: String) {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            File(filePath)
        )
        openPdfIntent.setDataAndType(fileUri, "application/pdf")
        openPdfIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            openPdfIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_rasso)
            .setContentTitle("PDF Criado")
            .setContentText("$fileName.pdf foi criado em $filePath")
            .setContentIntent(pendingIntent) // Set PendingIntent
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(requireContext())) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(1, builder.build())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            STORAGE_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    savePDF()
                }else{
                    Toast.makeText(requireContext(), "Permissão negada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}