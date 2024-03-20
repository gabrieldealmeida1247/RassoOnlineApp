package com.example.rassoonlineapp.Model

data class Proposals(
    var proposalId: String? = "",
    var userId: String? = "",
    var username: String? = "",  // Adiciona a propriedade para o nome de usuário
    var profileImage: String? = "",  // Adiciona a propriedade para a imagem do usuário
    var descricao: String? = "",
    var lance: String? = "",
    var numberDays: String? = "",
) {
}