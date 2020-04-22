package com.ssquare.myapplication.monokrome.main.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "magazines")
data class Magazine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String = "",
    val title: String = "",
    val description: String = "",
    val releaseDate: Long = 0,
    val imageUrl: String = "",
    val editionUrl: String = ""
)
