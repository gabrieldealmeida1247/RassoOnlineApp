package com.example.rassoonlineapp.Model

data class ServiceContractCount(
    var userId: String = "",
    var concludeCount: Int,
    var cancelCount : Int
){
    constructor():this("",0,0)
}
