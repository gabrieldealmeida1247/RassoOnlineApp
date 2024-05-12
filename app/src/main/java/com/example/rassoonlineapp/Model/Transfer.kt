package com.example.rassoonlineapp.Model

data class Transfer(
    val senderUsername: String,
    val senderId: String,
    val receiverId: String,
    val amount: Double,
    val timestamp: Long // Pode ser útil para ordenar as transferências
){
    constructor(): this("","","",0.0,0L)
}
