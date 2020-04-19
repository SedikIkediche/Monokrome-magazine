package com.ssquare.myapplication.monokrome.main.data

data class Magazine(
    val id: Long,
    val title: String,
    val description: String,
    val releaseDate: Long,
    val imageUrl: String,
    val editionUrl: String
) {
}