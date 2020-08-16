package com.ssquare.myapplication.monokrome.network

data class MagazineOrException(
    val magazine: NetworkMagazine?,
    val exception: Exception?
) {
    init {
        if (magazine == null && exception == null) {
            throw IllegalArgumentException("Both data and exception can't be null")
        } else if (magazine != null && exception != null) {
            throw IllegalArgumentException("Both data and exception can't be non-null")
        }
    }
}