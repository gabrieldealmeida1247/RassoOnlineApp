package com.example.rassoonlineapp.Admin.model

data class AdminNotification(

    val notificationId: String,
    val textoFeed: String,
){
    constructor() : this("", "")
}
