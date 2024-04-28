package com.example.rassoonlineapp.Admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.rassoonlineapp.Admin.model.AdminNotification
import com.example.rassoonlineapp.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminNotificationsFragment: Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var textViewFeed: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.admin_fragment_notifications, container, false)

        databaseReference = FirebaseDatabase.getInstance().reference.child("notifications")

        textViewFeed = view.findViewById(R.id.textView_feed)

        val buttonEnviar: Button = view.findViewById(R.id.button_enviar)
        buttonEnviar.setOnClickListener {
            val textoFeed = textViewFeed.text.toString().trim()

            if (textoFeed.isNotEmpty()) {
                // Criando um ID único para a notificação
                val notificationId = databaseReference.push().key
                if (notificationId != null) {
                    // Criando um objeto AdminNotification com os dados
                    val adminNotification = AdminNotification(notificationId, textoFeed)

                    // Enviando os dados para o Firebase Realtime Database
                    databaseReference.child(notificationId).setValue(adminNotification)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Limpar o EditText após o envio bem-sucedido
                                textViewFeed.setText("")
                                // Feedback para o usuário, se necessário
                            } else {
                                // Tratar caso de falha no envio
                            }
                        }
                }
            } else {
                // Caso o texto esteja vazio, fornecer feedback ao usuário
            }
        }

        return view
    }
}

