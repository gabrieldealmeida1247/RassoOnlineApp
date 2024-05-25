package com.example.rassoonlineapp.View

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rassoonlineapp.Model.Hire
import com.example.rassoonlineapp.Model.HireStats
import com.example.rassoonlineapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
                    // Atualizar o contador de contratações (hires) feitas
                    updateHireStats(userId)
                    // Atualizar o contador de contratações recebidas pelo outro usuário
                updateHireReceiveStats(userIdOther?: "")

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
            Toast.makeText(this, "Por favor, preencha todos os campos corretamente.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHireStats(userId: String) {
        val db = FirebaseDatabase.getInstance().reference
        val statsRef = db.child("HireStats").child(userId)

        statsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var hireStats = snapshot.getValue(HireStats::class.java)
                if (hireStats == null) {
                    hireStats = HireStats(userId = userId, totalHires = 1)
                } else {
                    hireStats.totalHires += 1
                }

                statsRef.setValue(hireStats)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }


    private fun updateHireReceiveStats(userId: String) {
        val statsRef = FirebaseDatabase.getInstance().reference.child("HireStats").child(userId)

        statsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val hireStats = dataSnapshot.getValue(HireStats::class.java) ?: HireStats(userId = userId)

                    hireStats.totalHiresReceive++

                statsRef.setValue(hireStats).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                       // Toast.makeText(mContext, if (accepted) "Projeto aceito." else "Projeto recusado.", Toast.LENGTH_SHORT).show()
                    } else {
                        //Toast.makeText(mContext, "Erro ao atualizar as estatísticas: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

}
