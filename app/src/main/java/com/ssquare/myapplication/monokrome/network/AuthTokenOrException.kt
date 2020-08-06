package com.ssquare.myapplication.monokrome.network

import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.User

data class AuthTokenOrException(
    val authToken: String?,
    val exception: Exception?
) {
    init {
        if (authToken == null && exception == null) {
            throw IllegalArgumentException("Both data and exception can't be null")
        } else if (authToken != null && exception != null) {
            throw IllegalArgumentException("Both data and exception can't be non-null")
        }
    }
}