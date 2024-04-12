package com.example.rassoonlineapp.Model

data class ProposalsStatistic(
    val statisticId: String = "",
    val userId: String = "",
    val userIdOther:String = "",
    val postId: String = "",
    var proposalsCount: Int = 0,
    var proposalsRefuseCount: Int = 0,
    var proposalsReceiveCount: Int = 0,
    var proposalsAcceptCount: Int = 0
    )
