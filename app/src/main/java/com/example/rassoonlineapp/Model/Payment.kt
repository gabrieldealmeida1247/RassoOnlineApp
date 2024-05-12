package com.example.rassoonlineapp.Model

data class Payment(
    val id: String,
    val userId: String,
    val amount: Double,
    val date: String
)
{
    constructor():this(
        "",
        "",
        0.0,
        ""
    )
}
