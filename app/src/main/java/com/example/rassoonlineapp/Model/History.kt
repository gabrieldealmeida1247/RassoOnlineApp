package com.example.rassoonlineapp.Model

data class History(
    var historyId: String? = "",
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
    var data_hora: String? = "",
)


data class ManageServiceHistory(
    var serviceHistoryId:String = "",
    var serviceId: String = "",
    var proposalId: String = "",
    var userIdOther:String ="",
    val postId: String = "",
    var userId: String = "",
    var status: String = "",
    var money: String  = "",
    var projectDate: String = "",
    var workerName: String = "",
    var clientName:String = "",
    var projectName: String = "",
    var expirationDate: String = ""
)
