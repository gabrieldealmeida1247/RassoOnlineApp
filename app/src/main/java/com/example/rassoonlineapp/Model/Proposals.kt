package com.example.rassoonlineapp.Model

data class Proposals(
    var proposalId: String? = "",
    var userId: String? = "",
    var serviceId: String? = "",
    var postId: String? = "",
    var projectTitle: String? = "",
    var descricao: String? = "",
    var lance: String? = "",
    var numberDays: String? = "",
    var username: String? = "", // Adicionando o campo para o nome de usuário
    var profileImage: String? = "", // Adicionando o campo para a imagem do perfil
    var accepted: String = "",
    var rejected: String = "",
    var prazoAceitacao: String = ""
)

data class ManageService(
    var serviceId: String = "",
    var proposalId: String = "",
    var userId: String = "",
    var status: String = "",
    var money: String  = "",
    var projectDate: String = "",
    var workerName: String = "",
    var clientName:String = "",
    var projectName: String = "",
    var expirationDate: String = ""
)

data class ManageProject(
    var manageId: String = "",
    var serviceId: String = "",
    var proposalId: String = "",
    var userId: String = "",
    var postId: String = "",
    var projectName: String = "",
    var description: String = "",
    var skills: List<String>? = null,
    var workerName: String = "",
    var clientName:String = "",
    var prazo: String = "",
    var prazoTermino: String = "",
    var tempoRestante: String = "",
    var pay: String = "",
    var status: String = ""


)


