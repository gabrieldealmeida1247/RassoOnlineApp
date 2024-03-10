package com.example.rassoonlineapp.Model
data class Post(
    var postId: String? = "",
    var userId: String? = "",
    var userName: String? = "",  // Adiciona a propriedade para o nome de usuário
    var userProfileImage: String? = "",  // Adiciona a propriedade para a imagem do usuário
    var titulo: String? = "",
    var descricao: String? = "",
    var habilidades: List<String>? = null,
    var orcamento: String? = "",
    var prazo: String? = "",
    var tipoTrabalho: String? = "",
    var data_hora: String? = ""
) {
    // Restante do código...
}
