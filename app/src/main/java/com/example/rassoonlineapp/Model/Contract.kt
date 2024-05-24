package com.example.rassoonlineapp.Model

data class Contract(
        var contractId: String = "",
        var hireId: String = "",
        val userId: String = "",
        val userIdOther: String = "",
        var projectName: String = "",
        var workerName: String = "",
        var clientName: String = "",
        var money: String = "",
        var projectDate: String = "",
        var expirationDate: String = "",
        var status: String = "",
        var description: String = ""
){

                constructor():this("","","","",
                        "","",
                        "","","",
                        "","","")
}




