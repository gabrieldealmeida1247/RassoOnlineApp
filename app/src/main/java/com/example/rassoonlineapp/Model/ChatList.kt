package com.example.rassoonlineapp.Model

data class ChatList(
    var senderId: String? = "",
    var receiverId: String? = "",
    var message: String? = "",
    var user: User? = null // Adicionando referência ao usuário associado ao chat

) {
}