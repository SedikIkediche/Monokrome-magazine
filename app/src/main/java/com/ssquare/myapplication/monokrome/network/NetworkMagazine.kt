package com.ssquare.myapplication.monokrome.network

data class NetworkMagazine(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val releaseDate: Long = 0,
    val imagePath: String = "",
    val pdfPath: String = ""
)