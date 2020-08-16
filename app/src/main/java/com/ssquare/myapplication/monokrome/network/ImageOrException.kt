package com.ssquare.myapplication.monokrome.network

data class ImageOrException(
    val image: NetworkImage?,
    val exception: Exception?
) {
    init {
        if (image == null && exception == null) {
            throw IllegalArgumentException("Both data and exception can't be null")
        } else if (image != null && exception != null) {
            throw IllegalArgumentException("Both data and exception can't be non-null")
        }
    }
}