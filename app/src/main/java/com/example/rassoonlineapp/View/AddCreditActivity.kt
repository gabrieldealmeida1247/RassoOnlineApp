package com.example.rassoonlineapp.View

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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.rassoonlineapp.API.ApiUtilities
import com.example.rassoonlineapp.Model.Payment
import com.example.rassoonlineapp.Model.transferAmount
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.Utils.Stripe.PUBLISHEBLE_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AddCreditActivity : AppCompatActivity() {

    lateinit var paymentSheet: PaymentSheet
    lateinit var customerId: String
    lateinit var ephemeralKey: String
    var clientSecret: String? = null

    private val STORAGE_CODE = 1001
    private val CHANNEL_ID = "PDF_NOTIFICATION_CHANNEL"

    private lateinit var editTextAmount: EditText
    private lateinit var textViewAmount: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_credit)

        PaymentConfiguration.init(this, PUBLISHEBLE_KEY)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        editTextAmount = findViewById(R.id.edit_amount)
        textViewAmount = findViewById(R.id.textView_amount)
val btn_generate_pdf = findViewById<Button>(R.id.btn_generate_pdf).setOnClickListener {
    generateReportPDF()
}
        getCustomerId()
        val buttonAdd = findViewById<Button>(R.id.add)
        buttonAdd.setOnClickListener {
            val amount = editTextAmount.text.toString()
            if (amount.isNotEmpty()) {
                getPaymentIntent(amount)
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }
            loadPaidAmount()

    }

    private fun paymentFlow() {
        clientSecret?.let { secret ->
            paymentSheet.presentWithPaymentIntent(
                secret,
                PaymentSheet.Configuration(
                    "Papaya Coders",
                    PaymentSheet.CustomerConfiguration(
                        customerId, ephemeralKey
                    )
                )
            )
            Log.d("PaymentFlow", "Payment flow started with Client Secret: $secret")
        } ?: run {
            Toast.makeText(this@AddCreditActivity, "Client Secret is null", Toast.LENGTH_SHORT).show()
        }
    }

    private var apiInterface = ApiUtilities.getApiInterface()

    private fun getCustomerId() {
        lifecycleScope.launch(Dispatchers.IO){
            val res = apiInterface.getCustomer()
            withContext(Dispatchers.Main){
                if (res.isSuccessful && res.body() != null){
                    customerId = res.body()!!.id
                    Log.d("AddCreditActivity", "Customer ID: $customerId")
                    getEphemeralKey(customerId)
                }
            }
        }
    }

    private fun getEphemeralKey(customerId: String) {
        lifecycleScope.launch(Dispatchers.IO){
            val res = apiInterface.getEphemeralKey(customerId)
            withContext(Dispatchers.Main){
                if (res.isSuccessful && res.body() != null){
                    ephemeralKey = res.body()!!.secret // Alteração aqui para pegar o segredo da chave efêmera
                    Log.d("AddCreditActivity", "Customer ID: $customerId")
                    getPaymentIntent(customerId)
                }
            }
        }
    }


    private fun getPaymentIntent(amount: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val res = apiInterface.getPaymentIntent(customerId, amount)
            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    clientSecret = res.body()!!.client_secret
                    Toast.makeText(
                        this@AddCreditActivity,
                        "Proceed for payment",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("AddCreditActivity", "Client Secret: $clientSecret")
                    paymentFlow()
                }
            }
        }
    }



    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        if (paymentSheetResult is PaymentSheetResult.Completed) {
            Toast.makeText(this@AddCreditActivity, "Payment Done", Toast.LENGTH_SHORT).show()
            val userId = getCurrentUserId()
            val paidAmount = editTextAmount.text.toString()
            savePaidAmount(paidAmount)
            savePaidAmountToDatabase(userId,paidAmount)
            // Atualize o textViewAmount com o novo total
            loadPaidAmount()

            Log.d("PaymentResult", "Payment Done")
            // Atualizar textViewAmount com o valor do EditText após o pagamento ser concluído
           // textViewAmount.text = editTextAmount.text.toString()
            savePaymentToDatabase(userId, paidAmount)
        } else if (paymentSheetResult is PaymentSheetResult.Failed) {
            // Exibir mensagem de erro para o usuário
            val errorMessage = paymentSheetResult.error?.message ?: "Unknown error"
            Toast.makeText(this@AddCreditActivity, "Payment Failed: $errorMessage", Toast.LENGTH_SHORT).show()
            Log.d("PaymentResult", "Payment Failed: $errorMessage")
        }

    }


    private fun savePaidAmount(amount: String) {
        val userId = getCurrentUserId()
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val currentAmount = sharedPreferences.getString("paidAmount_$userId", "0")?.toDoubleOrNull() ?: 0.0
        val newAmount = amount.toDoubleOrNull() ?: 0.0
        val totalAmount = currentAmount + newAmount

        editor.putString("paidAmount_$userId", totalAmount.toString()) // Use o UID como parte da chave
        editor.apply()

        textViewAmount.text = totalAmount.toString() // Atualiza o textView com o novo total
    }


    private fun loadPaidAmount() {
        val userId = getCurrentUserId()
        val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")

        databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalAmount = 0.0
                for (childSnapshot in dataSnapshot.children) {
                    val transferData = childSnapshot.getValue(transferAmount::class.java)
                    transferData?.let {
                        totalAmount += it.amount
                    }
                }
                textViewAmount.text = totalAmount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("AddCreditActivity", "Failed to load paid amount from database: ${databaseError.message}")
            }
        })
    }


    private fun getCurrentUserId(): String {
        val firebaseAuth = FirebaseAuth.getInstance()
        return firebaseAuth.currentUser?.uid ?: ""
    }



    private fun savePaidAmountToDatabase(userId: String, amount: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")

        // Consulte o banco de dados para verificar se já existe um valor para este usuário
        databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Se o usuário já tiver um valor registrado, atualize-o
                    for (childSnapshot in dataSnapshot.children) {
                        val transferData = childSnapshot.getValue(transferAmount::class.java)
                        if (transferData != null) {
                            val newAmount = transferData.amount + amount.toDouble()
                            childSnapshot.ref.child("amount").setValue(newAmount)
                                .addOnSuccessListener {
                                    Log.d("AddCreditActivity", "Amount updated successfully in the database")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("AddCreditActivity", "Failed to update amount in the database: ${e.message}")
                                }
                        }
                    }
                } else {
                    // Se o usuário não tiver um valor registrado, crie um novo
                    val transferId = databaseReference.push().key ?: ""
                    if (transferId.isNotEmpty()) {
                        val transferData = transferAmount(transferId, userId, amount.toDouble())
                        databaseReference.child(transferId).setValue(transferData)
                            .addOnSuccessListener {
                                Log.d("AddCreditActivity", "Transfer saved successfully to the database")
                            }
                            .addOnFailureListener { e ->
                                Log.e("AddCreditActivity", "Failed to save transfer to the database: ${e.message}")
                            }
                    } else {
                        Log.e("AddCreditActivity", "Failed to generate unique transfer ID")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("AddCreditActivity", "Database query cancelled: ${databaseError.message}")
            }
        })
    }
    private fun generateReportPDF() {
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
            reportData.append("Relatório de Pagamentos\n\n")

            // Recupere o ID do usuário atualmente logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Verifique se o usuário está logado
            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("payments")
                databaseReference.orderByChild("userId").equalTo(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (paymentSnapshot in dataSnapshot.children) {
                                val payment = paymentSnapshot.getValue(Payment::class.java)
                                payment?.let {
                                    // Adicione os dados do pagamento ao relatório
                                    reportData.append("Data do Pagamento: ${payment.date}\n")
                                    reportData.append("Valor do Pagamento: ${payment.amount}\n\n")
                                }
                            }

                            // Adicione os dados do relatório ao PDF
                            mDoc.add(Paragraph(reportData.toString()))
                            mDoc.close()

                            // Exibir notificação
                            showNotification(mFileName, mFilePath)
                            Toast.makeText(this@AddCreditActivity, "$mFileName.pdf\n foi criado em \n$mFilePath", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("AddCreditActivity", "Failed to retrieve payments from database: ${databaseError.message}")
                        }
                    })
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao criar PDF: ${e.toString()}", Toast.LENGTH_SHORT).show()
            Log.e("PDF", "Erro ao criar PDF", e)
        }
    }


    private fun showNotification(fileName: String, filePath: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            this,
            "${packageName}.fileprovider",
            File(filePath)
        )
        openPdfIntent.setDataAndType(fileUri, "application/pdf")
        openPdfIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openPdfIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_rasso)
            .setContentTitle("PDF Criado")
            .setContentText("$fileName.pdf foi criado em $filePath")
            .setContentIntent(pendingIntent) // Set PendingIntent
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ContextCompat.checkSelfPermission(
                    this@AddCreditActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(1, builder.build())
        }
    }



    private fun savePaymentToDatabase(userId: String, amount: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("payments")

        // Crie um ID único para cada pagamento
        val paymentId = databaseReference.push().key ?: ""

        // Verifique se o ID do usuário é válido
        if (userId.isNotEmpty() && paymentId.isNotEmpty()) {
            // Obtenha a data atual
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Crie um objeto Payment com os dados do pagamento
            val payment = Payment(paymentId, userId, amount.toDouble(), currentDate)

            // Salve o pagamento no Firebase Database
            databaseReference.child(paymentId).setValue(payment)
                .addOnSuccessListener {
                    Log.d("AddCreditActivity", "Payment saved successfully to the database")
                }
                .addOnFailureListener { e ->
                    Log.e("AddCreditActivity", "Failed to save payment to the database: ${e.message}")
                }
        } else {
            Log.e("AddCreditActivity", "Failed to generate payment ID or user ID")
        }
    }


}
