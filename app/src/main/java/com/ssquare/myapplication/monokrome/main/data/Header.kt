package com.ssquare.myapplication.monokrome.main.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "header")
data class Header(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageUrl: String = ""
) {
}