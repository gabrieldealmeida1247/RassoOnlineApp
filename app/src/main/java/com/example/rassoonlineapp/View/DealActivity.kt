package com.example.rassoonlineapp.View

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rassoonlineapp.Model.Hire
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class DealActivity : AppCompatActivity() {

    private var firebaseUser: FirebaseUser? = null
    private lateinit var projectNameEditText: EditText
    private lateinit var commentsEditText: EditText
    private lateinit var editPriceEditText: EditText
    private lateinit var publishButton: Button
    private var userIdOther: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        projectNameEditText = findViewById(R.id.project_name)
        commentsEditText = findViewById(R.id.comments)
        editPriceEditText = findViewById(R.id.edit_price)
        publishButton = findViewById(R.id.publish_btn)

        userIdOther = intent.getStringExtra("userIdOther")

        publishButton.setOnClickListener {
            saveHireDetails()
        }
    }

    private fun saveHireDetails() {
        val projectName = projectNameEditText.text.toString().trim()
        val comments = commentsEditText.text.toString().trim()
        val editPrice = editPriceEditText.text.toString().toDoubleOrNull() ?: 0.0

        if (projectName.isNotEmpty() && comments.isNotEmpty() && editPrice > 0) {
            val db = FirebaseDatabase.getInstance().reference
            val hireId = db.child("hires").push().key ?: UUID.randomUUID().toString()
            val userId = firebaseUser?.uid ?: ""

            val hire = Hire(
                hireId = hireId,
                userId = userId,
                userIdOther = userIdOther ?: "",
                projectName = projectName,
                comments = comments,
                editPrice = editPrice,
                timestamp = System.currentTimeMillis() // Adiciona o timestamp
            )

            db.child("hires").child(hireId).setValue(hire)
                .addOnSuccessListener {
                    // Ação ao sucesso
                    Toast.makeText(this, "Dados enviados com sucesso!", Toast.LENGTH_SHORT).show()
                    projectNameEditText.text.clear()
                    commentsEditText.text.clear()
                    editPriceEditText.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Dados Não foram enviados por favor tente mais tarde!", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Notifique o usuário para preencher todos os campos corretamente
        }
    }
}
