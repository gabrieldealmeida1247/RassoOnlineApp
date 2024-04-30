package com.example.rassoonlineapp.View

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.rassoonlineapp.R
import com.google.firebase.database.FirebaseDatabase

class InaproprieteContentActivity : AppCompatActivity() {
    private lateinit var inputName: EditText
    private lateinit var inputSubject: EditText
    private lateinit var inputMessage: EditText
    private lateinit var btnSubmit: Button

    private lateinit var postId: String // Você precisará definir postId quando puxar da tabela Post

    private val database = FirebaseDatabase.getInstance()
    private val inappropriateContentRef = database.getReference("inappropriate_content")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inapropriete_content)

        inputName = findViewById(R.id.input_name)
        inputSubject = findViewById(R.id.input_subject)
        inputMessage = findViewById(R.id.input_message)
        btnSubmit = findViewById(R.id.btn_submit)

        // Recebendo o postId passado como extra
        postId = intent.getStringExtra("postId") ?: ""
        if (postId.isEmpty()) {
            // Faça algo se o postId estiver vazio ou não for passado
        }

        btnSubmit.setOnClickListener {
            enviarConteudoInapropriado()
        }
    }

    private fun enviarConteudoInapropriado() {
        val name = inputName.text.toString().trim()
        val subject = inputSubject.text.toString().trim()
        val message = inputMessage.text.toString().trim()

        if (name.isEmpty() || subject.isEmpty() || message.isEmpty()) {
            // Faça algo se algum dos campos estiver vazio
            return
        }
        val contentId = inappropriateContentRef.push().key

        val inappropriateContent = hashMapOf(
            "contentId" to contentId,
            "name" to name,
            "subject" to subject,
            "message" to message,
            "postId" to postId // Certifique-se de definir o postId antes de enviar
        )

        inappropriateContentRef.push().setValue(inappropriateContent)
            .addOnSuccessListener {
                // Dados enviados com sucesso
                limparCampos()
            }
            .addOnFailureListener {
                // Ocorreu um erro ao enviar dados
            }
    }

    private fun limparCampos() {
        inputName.text.clear()
        inputSubject.text.clear()
        inputMessage.text.clear()
    }
}
