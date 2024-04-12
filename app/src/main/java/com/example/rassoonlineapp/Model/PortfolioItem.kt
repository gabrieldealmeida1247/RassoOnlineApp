package com.example.rassoonlineapp.Model

data class PortfolioItem(
    val portfolioId:String,
    val images: List<String>, // Lista de URLs das imagens
    val videos: List<String> // Lista de URLs dos vídeos
)
