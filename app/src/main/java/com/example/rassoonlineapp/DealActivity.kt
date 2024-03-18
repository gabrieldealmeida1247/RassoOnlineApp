package com.example.rassoonlineapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class DealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal)

      val  budgetSpinner = findViewById<Spinner>(R.id.budget_spinner)
       val receiveOffersCheckbox = findViewById<CheckBox>(R.id.receive_offers_checkbox)

        // Definindo as opções do spinner
        val budgetOptions = arrayOf("Menos de USD 50", "USD 50 - USD 100", "Mais de USD 100")

        // Criando um adaptador para o spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, budgetOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Definindo o adaptador para o spinner
        budgetSpinner.adapter = adapter

        // Definindo um ouvinte de seleção de item para o spinner
        budgetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Aqui você pode exibir o conteúdo com base no item selecionado no spinner
                val selectedOption = budgetOptions[position]
                // Exemplo de ação com base na seleção
                // exibirConteudoSelecionado(selectedOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Implementação opcional caso nenhum item seja selecionado
            }
        }
    }
}