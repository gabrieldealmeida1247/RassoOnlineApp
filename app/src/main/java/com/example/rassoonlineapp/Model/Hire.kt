package com.example.rassoonlineapp.Model

data class Hire(
    val hireId: String = "",
    val userId: String = "",
    val userIdOther: String = "",
    val projectName: String = "",
    val comments: String = "",
    val editPrice: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis() // Novo campo para armazenar o timestamp
)
