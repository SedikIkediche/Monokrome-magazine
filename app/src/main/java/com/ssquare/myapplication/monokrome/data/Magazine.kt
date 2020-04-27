package com.ssquare.myapplication.monokrome.data

data class Magazine(
    val id: Long = 0,
    val path: String = "",
    val title: String = "",
    val description: String = "",
    val releaseDate: Long = 0,
    val imageUrl: String = "",
    val editionUrl: String = ""
)
