package com.example.rassoonlineapp.Model

data class Transacao(
    val userIdOther: String = "",
    val userId: String = "", //id do usuário atual
    val remetente: String = "",  // Chave do remetente
    val destinatario: String = "",  // Chave do destinatário
    val valor: Double = 0.0,  // Valor transferido
    val timestamp: Long = System.currentTimeMillis()  // Timestamp da transação
)
