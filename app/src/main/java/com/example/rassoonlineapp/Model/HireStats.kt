package com.example.rassoonlineapp.Model

data class HireStats(
    var accepted: Int = 0,
    var refused: Int = 0,
    var acceptedC: Int = 0,
    var refusedC: Int = 0,
    var totalHires: Int = 0,
    var totalHiresReceive: Int = 0,
    var totalHiresDelete: Int = 0,
    var userId: String = ""
)
