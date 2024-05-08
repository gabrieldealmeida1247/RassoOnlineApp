package com.example.rassoonlineapp.Model

data class transferAmount(
    val tranferId: String, //chave único
    val userId: String,
    var amount: Double
){
    constructor():this(
        "",
        "",
        0.0
    )
}
