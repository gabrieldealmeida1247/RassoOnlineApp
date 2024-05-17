package com.example.rassoonlineapp.Admin.model

data class UserCount(
    val count: Int, // Campo para armazenar a contagem de usuários
    val deleteCount: Int,
    val bannedUserCount: Int,
    val loggedInCount: Int,
    val loggedOutCount: Int
) {
    constructor() : this(0, 0,0,0,0) // Construtor vazio sem argumentos
}
