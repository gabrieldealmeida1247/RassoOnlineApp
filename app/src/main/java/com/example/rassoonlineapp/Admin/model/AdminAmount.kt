package com.example.rassoonlineapp.Admin.model

data class AdminAmount(
  var adminAmount: Double = 0.0 // Valor padrão para evitar erros de leitura
) {
  // Construtor vazio necessário para Firebase Database
  constructor() : this(0.0)
}
