package com.example.rassoonlineapp.Model

data class ManageContract(
    var manageContractId: String,
    var contractId: String = "",
    val userId: String = "",
    val userIdOther: String = "",
    var projectDate: String = "",
    var expirationDate: String = "",
    var tempoRestante: String = "",
    var projectName: String = "",
    var workerName: String = "",
    var clientName: String = "",
    var status: String = "",
    var money: String = "",
    var description: String = "",
    var isCompleted: Boolean = false,
    var isCancelled: Boolean = false,
    var progressValue: Int = 0
){
    constructor():this("","","","",
        "","","","",
        "","","","","",false,false,0)
}
