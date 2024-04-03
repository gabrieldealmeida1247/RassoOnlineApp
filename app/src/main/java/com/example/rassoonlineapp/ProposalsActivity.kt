package com.example.rassoonlineapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rassoonlineapp.Adapter.ProposalsSingleItemAdapter
import com.example.rassoonlineapp.Model.Proposals
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class ProposalsActivity : AppCompatActivity(), ProposalsSingleItemAdapter.ProposalAcceptListener {
    private lateinit var adapter: ProposalsSingleItemAdapter
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var postId: String
    private lateinit var projectTitle: String
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var proposalsReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proposals)

        val proposalsList: MutableList<Proposals>? = null
        if (proposalsList != null) {
            adapter = ProposalsSingleItemAdapter(proposalsList)
            adapter.setAcceptListener(this)
        } else {
            // Trate a lista nula de acordo com a lógica do seu aplicativo
        }
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()
        proposalsReference = firebaseDatabase.reference.child("Proposals")

        postId = intent.getStringExtra("postId") ?: ""
        projectTitle = intent.getStringExtra("projectTitle") ?: ""

        val bidAmountInput = findViewById<EditText>(R.id.bid_amount)
        val finalAmountSpan = findViewById<TextView>(R.id.paid_to_you)
        val deliveryTimeInput = findViewById<EditText>(R.id.delivery_time)
        val descriptionInput = findViewById<EditText>(R.id.description)
        val sendButton = findViewById<Button>(R.id.send_button)

        bidAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed to implement this method
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed to implement this method
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val bidAmount = if (it.isNotEmpty()) it.toString().toDouble() else 0.0
                    val fee = bidAmount * 0.3
                    val paidAmount = bidAmount - fee

                    finalAmountSpan.text = "Pago a você: Kz ${
                        String.format("%.2f", bidAmount)
                    } - taxa de Kz ${String.format("%.2f", fee)} = Kz ${
                        String.format("%.2f", paidAmount)
                    }"
                }
            }
        })

        sendButton.setOnClickListener {
            val bidAmount = bidAmountInput.text.toString()
            val deliveryTime = deliveryTimeInput.text.toString()
            val description = descriptionInput.text.toString()

            if (validateInputs(bidAmount, deliveryTime, description)) {
                sendProposal(bidAmount, deliveryTime, description)
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(
        bidAmount: String,
        deliveryTime: String,
        description: String
    ): Boolean {
        return bidAmount.isNotEmpty() && deliveryTime.isNotEmpty() && description.isNotEmpty()
    }

    private fun sendProposal(bidAmount: String, deliveryTime: String, description: String) {
        val userId = firebaseUser.uid
        val proposalId = UUID.randomUUID().toString()

        val proposalsMap = HashMap<String, Any>()
        proposalsMap["proposalId"] = proposalId
        proposalsMap["userId"] = userId
        proposalsMap["postId"] = postId
        proposalsMap["projectTitle"] = projectTitle
        proposalsMap["descricao"] = description
        proposalsMap["lance"] = bidAmount
        proposalsMap["numberDays"] = deliveryTime
        proposalsMap["accepted"] = ""
        proposalsMap["rejected"] = ""

        val proposalRef = proposalsReference.child(proposalId)

        proposalRef.setValue(proposalsMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Proposta enviada com sucesso", Toast.LENGTH_SHORT).show()
                    Log.d("Firebase", "Proposta enviada com sucesso")
                    finish() // Fecha a atividade atual
                    // O envio do ManageService será tratado no ProposalsSingleItemAdapter.
                } else {
                    Toast.makeText(this, "Erro ao enviar a proposta", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onProposalAccepted(proposal: Proposals) {
        // Chame o método para criar o ManageService quando a proposta for aceita
        adapter.createManageService(proposal)
    }

    override fun onProposalRejected(proposal: Proposals) {
        //
    }
}
