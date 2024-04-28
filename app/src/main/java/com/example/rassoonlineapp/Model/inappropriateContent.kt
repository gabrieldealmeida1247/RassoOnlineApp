package com.example.rassoonlineapp.Model

data class inappropriateContent(
    val contentId: String,//chave da tabela
    val postId: String,//puxe o id do post da tabela Post e armazene nessa base de dados
    val name: String,
    val subject: String,
    val message: String
){
    constructor(): this("", "", "", "", "")
}
