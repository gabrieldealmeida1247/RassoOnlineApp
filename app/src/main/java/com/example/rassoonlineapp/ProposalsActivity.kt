package com.example.rassoonlineapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProposalsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proposals)

        val bidAmountInput = findViewById<EditText>(R.id.bid_amount)
        val finalAmountSpan = findViewById<TextView>(R.id.paid_to_you)

        bidAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Não é necessário implementar esse método
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Não é necessário implementar esse método
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

        val deliveryTimeInput = findViewById<EditText>(R.id.delivery_time)
        val descriptionInput = findViewById<EditText>(R.id.description)
        val sendButton = findViewById<Button>(R.id.send_button)

        sendButton.setOnClickListener {
            // Aqui você pode implementar a lógica para enviar a proposta,
            // usando os valores dos campos bidAmountInput, deliveryTimeInput e descriptionInput
            // Por exemplo:
            val bidAmount = bidAmountInput.text.toString()
            val deliveryTime = deliveryTimeInput.text.toString()
            val description = descriptionInput.text.toString()

            // Em seguida, você pode usar esses valores para enviar a proposta
            // para o servidor ou realizar outras operações necessárias.
        }

    }
}
