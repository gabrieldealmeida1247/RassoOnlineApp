package com.example.rassoonlineapp.Model

data class Rating(
    val ratingId: String = "",  // Adiciona valores padrão para os campos
    val userId: String = "",
    val userIdOther: String = "",
    val userName: String = "",
    val userProfileImageUrl: String = "",
    val rating: Double = 0.0,
    val description: String = ""

)
