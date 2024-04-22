package com.example.rassoonlineapp.Model

data class Statistic(
    val statisticId: String = "",
    val userId: String = "",
    val manageId: String = "",
    val postId: String = "",

    var postsCount: Int = 0,
    var serviceConclude: Int = 0,
    var serviceCancel: Int = 0,

    )
