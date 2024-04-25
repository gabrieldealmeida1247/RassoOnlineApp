package com.example.rassoonlineapp.View

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Adapter.TransactionAdapter
import com.example.rassoonlineapp.Model.Transacao
import com.example.rassoonlineapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryPaymentActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var recyclerViewTransactions: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_payment)

        database = FirebaseDatabase.getInstance()

        recyclerViewTransactions = findViewById<RecyclerView>(R.id.recyclerViewTransactions)
        recyclerViewTransactions.layoutManager = LinearLayoutManager(this)

        // Obter a suaChave e o ID do destinatário da Intent
        val suaChave = intent.getStringExtra("suaChave")
        val destinatario = intent.getStringExtra("destinatario")

        if (suaChave != null && suaChave.isNotEmpty()) {
            getTransactions(destinatario ?: suaChave) // Se o destinatario não estiver presente, usa suaChave como padrão
        } else {
            Toast.makeText(this, "Chave inválida", Toast.LENGTH_SHORT).show()
            finish()  // Finaliza a atividade se a chave não estiver presente
        }
    }

    private fun getTransactions(destinatario: String) {
        val ref = database.reference.child("transacoes")

        ref.orderByChild("destinatario").equalTo(destinatario).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactions = mutableListOf<Transacao>()
                for (postSnapshot in snapshot.children) {
                    val transaction = postSnapshot.getValue(Transacao::class.java)
                    if (transaction != null) {
                        transactions.add(transaction)
                    }
                }

                transactionAdapter = TransactionAdapter(transactions)
                recyclerViewTransactions.adapter = transactionAdapter
                transactionAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}

