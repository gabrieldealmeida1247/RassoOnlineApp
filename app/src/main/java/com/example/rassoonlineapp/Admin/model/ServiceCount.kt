package com.example.rassoonlineapp.Admin.model

data class ServiceCount(
    var postsCount: Int,
    var propCount: Int,
    var proposalsAcceptCount: Int,
    var proposalsRefuseCount: Int,
    var concludeCount: Int,
    var cancelCount : Int,
    var deleteCount: Int
){
    constructor():this(0,0,0,0,0,0,0)
}
