package com.example.rassoonlineapp.Model

data class Chat(
    var userName: String? = "",
    var userProfileImage: String? = "",
    var userId: String? = "",
    var user: User? = null // Adicionando referência ao usuário associado ao chat

) {
}