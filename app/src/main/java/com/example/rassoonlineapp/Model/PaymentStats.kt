package com.example.rassoonlineapp.Model

data class PaymentStats(
    val userId: String = "",
    var totalGanho: Double = 0.0,
    var totalPago: Double = 0.0, // Adicionado o campo totalPago
    var lastPaidAmount: Double = 0.0, // Adicionado o campo lastPaidAmount
    var lastGainedAmount: Double = 0.0 // Adicionado o campo lastGainedAmount
    )
