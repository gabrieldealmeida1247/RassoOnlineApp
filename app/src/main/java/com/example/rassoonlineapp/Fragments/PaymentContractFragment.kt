/*
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
import androidx.fragment.app.DialogFragment
import com.example.rassoonlineapp.Model.Transfer
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.Model.transferAmount
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentFragment : DialogFragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var editText_send_money: EditText
    private lateinit var editText_username: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        editText_send_money = view.findViewById(R.id.edit_send_amount)
        editText_username = view.findViewById<EditText?>(R.id.edit_text_username)

        val btn_generate_pdf = view.findViewById<Button>(R.id.btn_generate_pdf)
        btn_generate_pdf.setOnClickListener {
            generateReportPDF()
        }


        val sendButton = view.findViewById<Button>(R.id.send)

        sendButton.setOnClickListener {
            val userName = editText_username.text.toString()
            val amountToSubtract = editText_send_money.text.toString().toDoubleOrNull()

            // Verifica se o nome de usuário não está vazio
            if (userName.isNotBlank()) {
                if (amountToSubtract != null) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        subtractAmount(userId, amountToSubtract)
                        addAmountToUser(userName, amountToSubtract)

                        // Após as operações bem-sucedidas de subtração e adição, salve a transferência no banco de dados
                        saveTransferToDatabase(userId, userName, amountToSubtract)
                    } else {
                        Log.e("PaymentFragment", "Current user ID is null")
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid amount to subtract", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }



        return view
    }



    private fun subtractAmount(userId: String, amountToSubtract: Double) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")

        databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val transferData = childSnapshot.getValue(transferAmount::class.java)
                        if (transferData != null) {
                            val currentAmount = transferData.amount
                            val newAmount = currentAmount - amountToSubtract
                            childSnapshot.ref.child("amount").setValue(newAmount)
                                .addOnSuccessListener {
                                    // Atualize o textViewAmount com o novo saldo, se necessário
                                }
                                .addOnFailureListener { e ->
                                    Log.e("PaymentFragment", "Failed to update amount in the database: ${e.message}")
                                }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No transfer amount found for the current user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PaymentFragment", "Database query cancelled: ${databaseError.message}")
            }
        })
    }

    private fun addAmountToUser(userName: String, amountToAdd: Double) {
        findUserIdByName(userName) { userId ->
            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")
                databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (childSnapshot in dataSnapshot.children) {
                                val transferData = childSnapshot.getValue(transferAmount::class.java)
                                if (transferData != null) {
                                    val currentAmount = transferData.amount
                                    val newAmount = currentAmount + amountToAdd
                                    childSnapshot.ref.child("amount").setValue(newAmount)
                                        .addOnSuccessListener {
                                            // Atualize o textViewAmount com o novo saldo, se necessário
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("PaymentFragment", "Failed to update amount in the database: ${e.message}")
                                        }
                                }
                            }
                        } else {
                            // Se o usuário não tiver um valor registrado, crie um novo
                            val transferId = databaseReference.push().key ?: ""
                            if (transferId.isNotEmpty()) {
                                val transferData = transferAmount(transferId, userId, amountToAdd)
                                databaseReference.child(transferId).setValue(transferData)
                                    .addOnSuccessListener {
                                        Log.d("PaymentFragment", "Transfer saved successfully to the database")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("PaymentFragment", "Failed to save transfer to the database: ${e.message}")
                                    }
                            } else {
                                Log.e("PaymentFragment", "Failed to generate unique transfer ID")
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("PaymentFragment", "Database query cancelled: ${databaseError.message}")
                    }
                })
            } else {
                Toast.makeText(requireContext(), "User with username $userName not found", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun findUserIdByName(userName: String, callback: (String?) -> Unit) {
        val usersRef = database.child("Users")
        val query = usersRef.orderByChild("username").equalTo(userName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(User::class.java)
                        // Assuming each username is unique, so we only get the first user found
                        callback(user?.getUID())
                        return
                    }
                }
                callback(null) // User not found
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }


    private fun generateReportPDF() {
        val mDoc = Document()

        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())

        val mFilePath =
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" +
                    mFileName + ".pdf"

        try {
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))
            mDoc.open()

            // Adicione os dados do relatório ao PDF
            val reportData = StringBuilder()
            reportData.append("Relatório de Transferências\n\n")

            // Recupere o ID do usuário atualmente logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Recupere apenas as transferências do Firebase Database feitas pelo usuário atual
            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("transfers")
                databaseReference.orderByChild("senderId").equalTo(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (transferSnapshot in dataSnapshot.children) {
                                val transfer = transferSnapshot.getValue(Transfer::class.java)
                                transfer?.let {
                                    // Adicione os dados da transferência ao relatório
                                    reportData.append("Remetente: ${transfer.senderUsername}\n")
                                    reportData.append("Destinatário: ${transfer.receiverId}\n")
                                    reportData.append("Valor: ${transfer.amount}\n")
                                    reportData.append("Data: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                        Locale.getDefault()).format(Date(transfer.timestamp))}\n\n")
                                }
                            }

                            // Adicione os dados do relatório ao PDF
                            mDoc.add(Paragraph(reportData.toString()))
                            mDoc.close()

                            // Exibir notificação
                            showNotification(mFileName, mFilePath)
                            Toast.makeText(requireContext(), "$mFileName.pdf\n foi criado em \n$mFilePath", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("PaymentFragment", "Failed to retrieve transfers from database: ${databaseError.message}")
                        }
                    })
            }
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
                return
            }
            notify(1, builder.build())
        }
    }

    companion object {
        const val CHANNEL_ID = "PDF_CHANNEL"
    }

    private fun saveTransferToDatabase(senderId: String, receiverUsername: String, amount: Double) {
        getCurrentUserName(senderId) { senderUsername ->
            if (senderUsername != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("transfers")

                // Crie um ID único para cada transferência
                val transferId = databaseReference.push().key ?: ""

                // Verifique se os dados necessários são válidos
                if (transferId.isNotEmpty()) {
                    // Obtenha a data e hora atual
                    val timestamp = System.currentTimeMillis()

                    // Crie um objeto Transfer com os dados da transferência
                    val transfer = Transfer(senderUsername, senderId, receiverUsername, amount, timestamp)

                    // Salve a transferência no Firebase Database
                    databaseReference.child(transferId).setValue(transfer)
                        .addOnSuccessListener {
                            Log.d("PaymentFragment", "Transfer saved successfully to the database")
                        }
                        .addOnFailureListener { e ->
                            Log.e("PaymentFragment", "Failed to save transfer to the database: ${e.message}")
                        }
                } else {
                    Log.e("PaymentFragment", "Failed to generate transfer ID")
                }
            } else {
                Log.e("PaymentFragment", "Failed to retrieve sender's username")
            }
        }
    }

    private fun getCurrentUserName(userId: String, callback: (String?) -> Unit) {
        val userRef = database.child("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                callback(user?.getUsername())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PaymentFragment", "Failed to retrieve current user's name: ${databaseError.message}")
                callback(null)
            }
        })
    }



}
 */

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
import androidx.fragment.app.DialogFragment
import com.example.rassoonlineapp.Adapter.ManageProjectClientAdapter
import com.example.rassoonlineapp.Admin.model.AdminAmount
import com.example.rassoonlineapp.Model.ManageContract
import com.example.rassoonlineapp.Model.ManageProject
import com.example.rassoonlineapp.Model.ServiceContractCount
import com.example.rassoonlineapp.Model.StatisticContract
import com.example.rassoonlineapp.Model.Transfer
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.Model.transferAmount
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentContractFragment : DialogFragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var editText_send_money: EditText
    private lateinit var editText_username: EditText
    private lateinit var adapter: ManageProjectClientAdapter // Replace with your adapter class
    private var manageProjectList: List<ManageProject> = listOf() // Example initialization

    private var manageContractId: String? = null
    private var userId: String? = null
    private var workerName: String? = null
    private var firebaseUser: FirebaseUser? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_contract, container, false)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser

        // Initialize your adapter and set it to the RecyclerView
        adapter = ManageProjectClientAdapter(requireContext(), manageProjectList) // Pass the cont


        editText_send_money = view.findViewById(R.id.edit_send_amount_contract)
        editText_username = view.findViewById<EditText?>(R.id.edit_text_username_contract)


        // Obter os argumentos
        manageContractId = arguments?.getString("manageContractId")
        userId = arguments?.getString("userId")
        workerName = arguments?.getString("workerName")

        // Preencher o editText_username com o workName
        editText_username.setText(workerName)

        val btn_generate_pdf = view.findViewById<Button>(R.id.btn_generate_pdf_contract)
        btn_generate_pdf.setOnClickListener {
            generateReportPDF()
        }

        val sendButton = view.findViewById<Button>(R.id.send_contract)
/*
        sendButton.setOnClickListener {
            val userName = editText_username.text.toString()
            val amountToSubtract = editText_send_money.text.toString().toDoubleOrNull()

            // Verifica se o nome de usuário não está vazio
            if (userName.isNotBlank()) {
                if (amountToSubtract != null) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val adminAmount = amountToSubtract * 0.05
                        val amountAfterFee = amountToSubtract - adminAmount

                        subtractAmount(userId, amountAfterFee)
                        addAmountToUser(userName, amountAfterFee)
                        addAmountToAdmin(adminAmount)

                        // Após as operações bem-sucedidas de subtração e adição, salve a transferência no banco de dados
                        saveTransferToDatabase(userId, userName, amountToSubtract)

                        // Marcar como concluído
                        val currentManageProject = ManageContract(manageContractId ?: "", userId = userId)
                        handleConcluidoButtonClick(currentManageProject)


                        updateUI()
                    } else {
                        Log.e("PaymentFragment", "Current user ID is null")
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid amount to subtract", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }

 */

        sendButton.setOnClickListener {
            val userName = editText_username.text.toString()
            val amountToSubtract = editText_send_money.text.toString().toDoubleOrNull()

            // Verifica se o nome de usuário não está vazio
            if (userName.isNotBlank()) {
                if (amountToSubtract != null) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        getCurrentUserBalance { currentBalance ->
                            if (currentBalance != null && amountToSubtract <= currentBalance) {
                                val adminAmount = amountToSubtract * 0.05
                                val amountAfterFee = amountToSubtract - adminAmount

                                subtractAmount(userId, amountAfterFee)
                                addAmountToUser(userName, amountAfterFee)
                                addAmountToAdmin(adminAmount)

                                // Após as operações bem-sucedidas de subtração e adição, salve a transferência no banco de dados
                                saveTransferToDatabase(userId, userName, amountToSubtract)

                                // Marcar como concluído
                                val currentManageProject = ManageContract(manageContractId = manageContractId ?: "", userId = userId)
                                handleConcluidoButtonClick(currentManageProject)

                                updateUI()
                            } else {
                                Toast.makeText(requireContext(), "Saldo insuficiente. Adicione dinheiro à sua carteira.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e("PaymentFragment", "Current user ID is null")
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid amount to subtract", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }


    private fun getCurrentUserBalance(callback: (Double?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")
            databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            val transferData = childSnapshot.getValue(transferAmount::class.java)
                            if (transferData != null) {
                                callback(transferData.amount)
                                return
                            }
                        }
                    } else {
                        callback(null)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(null)
                }
            })
        } else {
            callback(null)
        }
    }



    private fun subtractAmount(userId: String, amountToSubtract: Double) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")

        databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val transferData = childSnapshot.getValue(transferAmount::class.java)
                        if (transferData != null) {
                            val currentAmount = transferData.amount
                            val newAmount = currentAmount - amountToSubtract
                            childSnapshot.ref.child("amount").setValue(newAmount)
                                .addOnSuccessListener {
                                    // Atualize o textViewAmount com o novo saldo, se necessário
                                    updateUI()

                                }
                                .addOnFailureListener { e ->
                                    Log.e("PaymentFragment", "Failed to update amount in the database: ${e.message}")
                                }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No transfer amount found for the current user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PaymentFragment", "Database query cancelled: ${databaseError.message}")
            }
        })
    }

    private fun updateUI() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")
            databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            val transferData = childSnapshot.getValue(transferAmount::class.java)
                            if (transferData != null) {
                                // Atualize o EditText com o novo saldo
                                editText_send_money.setText("")
                                editText_username.setText("")
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "No transfer amount found for the current user", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("PaymentFragment", "Database query cancelled: ${databaseError.message}")
                }
            })
        }
    }


    private fun addAmountToUser(userName: String, amountToAdd: Double) {
        findUserIdByName(userName) { userId ->
            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("transferAmounts")
                databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (childSnapshot in dataSnapshot.children) {
                                val transferData = childSnapshot.getValue(transferAmount::class.java)
                                if (transferData != null) {
                                    val currentAmount = transferData.amount
                                    val newAmount = currentAmount + amountToAdd
                                    childSnapshot.ref.child("amount").setValue(newAmount)
                                        .addOnSuccessListener {
                                            // Atualize o textViewAmount com o novo saldo, se necessário
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("PaymentFragment", "Failed to update amount in the database: ${e.message}")
                                        }
                                }
                            }
                        } else {
                            // Se o usuário não tiver um valor registrado, crie um novo
                            val transferId = databaseReference.push().key ?: ""
                            if (transferId.isNotEmpty()) {
                                val transferData = transferAmount(transferId, userId, amountToAdd)
                                databaseReference.child(transferId).setValue(transferData)
                                    .addOnSuccessListener {
                                        Log.d("PaymentFragment", "Transfer saved successfully to the database")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("PaymentFragment", "Failed to save transfer to the database: ${e.message}")
                                    }
                            } else {
                                Log.e("PaymentFragment", "Failed to generate unique transfer ID")
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("PaymentFragment", "Database query cancelled: ${databaseError.message}")
                    }
                })
            } else {
                Toast.makeText(requireContext(), "User with username $userName not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addAmountToAdmin(amountToAdd: Double) {
        val adminRef = FirebaseDatabase.getInstance().getReference("adminAmounts").child("admin")
        adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val adminAmountData = dataSnapshot.getValue(AdminAmount::class.java)
                    if (adminAmountData != null) {
                        val currentAmount = adminAmountData.adminAmount
                        val newAmount = currentAmount + amountToAdd
                        adminRef.child("adminAmount").setValue(newAmount)
                            .addOnSuccessListener {
                                Log.d("PaymentFragment", "Admin amount updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("PaymentFragment", "Failed to update admin amount in the database: ${e.message}")
                            }
                    }
                } else {
                    val adminAmountData = AdminAmount(amountToAdd)
                    adminRef.setValue(adminAmountData)
                        .addOnSuccessListener {
                            Log.d("PaymentFragment", "Admin amount set successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("PaymentFragment", "Failed to set admin amount in the database: ${e.message}")
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PaymentFragment", "Database query cancelled: ${databaseError.message}")
            }
        })
    }

    private fun findUserIdByName(userName: String, callback: (String?) -> Unit) {
        val usersRef = database.child("Users")
        val query = usersRef.orderByChild("username").equalTo(userName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(User::class.java)
                        // Assuming each username is unique, so we only get the first user found
                        callback(user?.getUID())
                        return
                    }
                }
                callback(null) // User not found
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                callback(null)
            }
        })
    }



    private fun generateReportPDF() {
        val mDoc = Document()

        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())

        val mFilePath =
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" +
                    mFileName + ".pdf"

        try {
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))
            mDoc.open()

            // Adicione os dados do relatório ao PDF
            val reportData = StringBuilder()
            reportData.append("Relatório de Transferências\n\n")

            // Recupere o ID do usuário atualmente logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Recupere apenas as transferências do Firebase Database feitas pelo usuário atual
            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("transfers")
                databaseReference.orderByChild("senderId").equalTo(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (transferSnapshot in dataSnapshot.children) {
                                val transfer = transferSnapshot.getValue(Transfer::class.java)
                                transfer?.let {
                                    // Adicione os dados da transferência ao relatório
                                    reportData.append("Remetente: ${transfer.senderUsername}\n")
                                    reportData.append("Destinatário: ${transfer.receiverId}\n")
                                    reportData.append("Valor: ${transfer.amount}\n")
                                    // Adicione a taxa de administração (5%) ao relatório
                                    val adminFee = transfer.amount * 0.05
                                    reportData.append("Taxa de Administração (5%): $adminFee\n")
                                    reportData.append("Valor após taxa: ${transfer.amount - adminFee}\n")
                                    reportData.append("Data: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                        Locale.getDefault()).format(Date(transfer.timestamp))}\n\n")
                                }
                            }

                            // Adicione os dados do relatório ao PDF
                            mDoc.add(Paragraph(reportData.toString()))
                            mDoc.close()

                            // Exibir notificação
                            showNotification(mFileName, mFilePath)
                            Toast.makeText(requireContext(), "$mFileName.pdf\n foi criado em \n$mFilePath", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("PaymentFragment", "Failed to retrieve transfers from database: ${databaseError.message}")
                        }
                    })
            }
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
                return
            }
            notify(1, builder.build())
        }
    }

    companion object {
        const val CHANNEL_ID = "PDF_CHANNEL"
    }

    private fun saveTransferToDatabase(senderId: String, receiverUsername: String, amount: Double) {
        getCurrentUserName(senderId) { senderUsername ->
            if (senderUsername != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("transfers")

                // Crie um ID único para cada transferência
                val transferId = databaseReference.push().key ?: ""

                // Verifique se os dados necessários são válidos
                if (transferId.isNotEmpty()) {
                    // Obtenha a data e hora atual
                    val timestamp = System.currentTimeMillis()

                    // Crie um objeto Transfer com os dados da transferência
                    val transfer = Transfer(senderUsername, senderId, receiverUsername, amount, timestamp)

                    // Salve a transferência no Firebase Database
                    databaseReference.child(transferId).setValue(transfer)
                        .addOnSuccessListener {
                            Log.d("PaymentFragment", "Transfer saved successfully to the database")
                        }
                        .addOnFailureListener { e ->
                            Log.e("PaymentFragment", "Failed to save transfer to the database: ${e.message}")
                        }
                } else {
                    Log.e("PaymentFragment", "Failed to generate transfer ID")
                }
            } else {
                Log.e("PaymentFragment", "Failed to retrieve sender's username")
            }
        }
    }

    private fun getCurrentUserName(userId: String, callback: (String?) -> Unit) {
        val userRef = database.child("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                callback(user?.getUsername())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PaymentFragment", "Failed to retrieve current user's name: ${databaseError.message}")
                callback(null)
            }
        })
    }


/*
    private fun handleConcluidoButtonClick(currentManageProject: ManageContract) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val manageProjectRef = databaseReference.child("ManageContracts").child(currentManageProject.manageContractId)

        manageProjectRef.child("status").setValue("Concluído")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentManageProject.status = "Concluído"
                    ManageCount(currentManageProject.userId)

                    // Incrementar o contador de serviços concluídos
                    val statisticRef = databaseReference.child("StatisticContract").child(firebaseUser!!.uid)

                    statisticRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val statistic = dataSnapshot.getValue(StatisticContract::class.java)

                            if (statistic != null) {
                                val updatedServiceConclude = statistic.serviceConclude + 1
                                statisticRef.child("serviceConclude").setValue(updatedServiceConclude)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Continue com as ações após a atualização bem-sucedida
                                            adapter.notifyDataSetChanged()
                                            Toast.makeText(context, "Status atualizado para Concluído", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle onCancelled
                        }
                    })

                    // Continue com as outras ações após a atualização do status
                    loadUserData(firebaseUser!!.uid) { userName, userProfileImage ->
                        // Enviar uma notificação para o usuário que fez a proposta

                      //  addNotification(currentManageProject.userId ?: "", currentManageProject.postId ?: "", userName, userProfileImage, currentManageProject.projectName ?: "")

                        // Atualize os detalhes do usuário que fez a proposta com os detalhes do usuário que aceitou a proposta
                        updateProposerUserDetails(currentManageProject.userId ?: "", userName, userProfileImage)

                        adapter.notifyDataSetChanged()
                    }

                    Toast.makeText(context, "Status atualizado para Concluído", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

 */
private fun handleConcluidoButtonClick(currentManageProject: ManageContract) {
    val databaseReference = FirebaseDatabase.getInstance().reference
    val manageProjectRef = databaseReference.child("ManageContracts").child(currentManageProject.manageContractId)

    manageProjectRef.child("status").setValue("Concluído")
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentManageProject.status = "Concluído"
                ManageCount(currentManageProject.userId)

                // Incrementar o contador de serviços concluídos
                val statisticRef = databaseReference.child("StatisticContract").child(firebaseUser!!.uid)

                statisticRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var serviceConcludeCount = 0
                        if (dataSnapshot.exists()) {
                            val statistic = dataSnapshot.getValue(StatisticContract::class.java)
                            serviceConcludeCount = statistic?.serviceConclude ?: 0
                        } else {
                            val newStatistic = StatisticContract(serviceCancel = 0, serviceConclude = 0)
                            statisticRef.setValue(newStatistic)
                        }
                        val updatedServiceConclude = serviceConcludeCount + 1
                        statisticRef.child("serviceConclude").setValue(updatedServiceConclude)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    adapter.notifyDataSetChanged()
                                    Toast.makeText(context, "Status atualizado para Concluído", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Erro ao acessar os dados de estatísticas: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })

                // Continue com as outras ações após a atualização do status
                loadUserData(firebaseUser!!.uid) { userName, userProfileImage ->
                    // Enviar uma notificação para o usuário que fez a proposta
                    // addNotification(currentManageProject.userId ?: "", currentManageProject.postId ?: "", userName, userProfileImage, currentManageProject.projectName ?: "")

                    // Atualize os detalhes do usuário que fez a proposta com os detalhes do usuário que aceitou a proposta
                    updateProposerUserDetails(currentManageProject.userId ?: "", userName, userProfileImage)

                    adapter.notifyDataSetChanged()
                }

                Toast.makeText(context, "Status atualizado para Concluído", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Erro ao atualizar o status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}

    private fun ManageCount(userId: String) {
        val postRef = FirebaseDatabase.getInstance().reference.child("ServiceContractCount").child(userId)
        postRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val statistic = dataSnapshot.getValue(ServiceContractCount::class.java)
                statistic?.let {
                    val concludeCount = it.concludeCount + 1
                    it.concludeCount = concludeCount
                    postRef.setValue(it)
                }
            } else {
                val service = ServiceContractCount(concludeCount = 1, cancelCount = 0)
                postRef.setValue(service)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao obter os dados das estatísticas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProposerUserDetails(userId: String, userName: String, userProfileImage: String?) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        val userUpdates = hashMapOf<String, Any>()
        userUpdates["userName"] = userName
        userUpdates["userProfileImage"] = userProfileImage ?: ""

        userRef.updateChildren(userUpdates)
            .addOnSuccessListener {
                Log.d("PaymentFragment", "Detalhes do usuário atualizados com sucesso.")
            }
            .addOnFailureListener { e ->
                Log.e("PaymentFragment", "Erro ao atualizar os detalhes do usuário.", e)
            }
    }

    private fun addNotification(userId: String, postId: String, userName: String, userProfileImage: String?, projectName: String) {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)
        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = firebaseUser!!.uid
        notiMap["postTitle"] = "Concluio o serviço: $projectName"
        notiMap["postId"] = postId
        notiMap["ispost"] = true
        notiMap["userName"] = userName
        notiMap["userProfileImage"] = userProfileImage ?: ""

        notiRef.push().setValue(notiMap)
    }

    private fun loadUserData(userId: String, callback: (String, String?) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    val userName = user?.getUsername() ?: ""
                    val userProfileImage = user?.getImage()

                    callback(userName, userProfileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}

