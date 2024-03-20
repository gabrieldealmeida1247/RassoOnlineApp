package com.example.rassoonlineapp
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class ProposalsActivity : AppCompatActivity() {
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proposals)

        firebaseUser = FirebaseAuth.getInstance().currentUser

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

                    finalAmountSpan.text = "Pago a você: Kz ${String.format("%.2f", bidAmount)} - taxa de Kz ${String.format("%.2f", fee)} = Kz ${String.format("%.2f", paidAmount)}"
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

    private fun validateInputs(bidAmount: String, deliveryTime: String, description: String): Boolean {
        return bidAmount.isNotEmpty() && deliveryTime.isNotEmpty() && description.isNotEmpty()
    }

    private fun sendProposal(bidAmount: String, deliveryTime: String, description: String) {
        val userId = firebaseUser?.uid ?: return // Get userId

        val proposalId = UUID.randomUUID().toString() // Generate unique proposalId

        val proposalsMap = HashMap<String, Any>()
        proposalsMap["proposalId"] = proposalId
        proposalsMap["userId"] = userId// You can set the userName later if needed // You can set the userProfileImage later if needed
        proposalsMap["descricao"] = description
        proposalsMap["lance"] = bidAmount
        proposalsMap["numberDays"] = deliveryTime

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Proposals")
        // Save Proposal Map to Firebase Database
        databaseReference.child(proposalId).setValue(proposalsMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Proposta enviada com sucesso", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Erro ao enviar a proposta", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
