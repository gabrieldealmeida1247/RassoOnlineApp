package com.example.rassoonlineapp.Model

data class StatisticContract(
    var statisticId: String = "",
    var userId: String = "",
    var manageContractId: String = "",
    var contractId: String = "",
    var serviceConclude: Int = 0,
    var serviceCancel: Int = 0,

    ){
}
