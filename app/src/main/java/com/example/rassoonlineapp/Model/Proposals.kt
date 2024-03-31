package com.example.rassoonlineapp.Model

data class Proposals(
    var proposalId: String? = "",
    var userId: String? = "",
    var postId: String? = "",
    var projectTitle: String? = "",
    var descricao: String? = "",
    var lance: String? = "",
    var numberDays: String? = "",
    var username: String? = "", // Adicionando o campo para o nome de usuário
    var profileImage: String? = "", // Adicionando o campo para a imagem do perfil
    var accepted: String = "",
    var rejected: String = ""
)
