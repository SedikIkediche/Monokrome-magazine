package com.ssquare.myapplication.monokrome.network

import com.ssquare.myapplication.monokrome.data.Header

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


data class MagazineListOrException(
    val magazineList: List<NetworkMagazine>?,
    val header: Header?,
    val exception: Exception?
) {
    init {
        if (magazineList == null && header == null && exception == null) {
            throw IllegalArgumentException("Both data and exception can't be null")
        } else if (magazineList != null && header == null && exception != null) {
            throw IllegalArgumentException("Both data and exception can't be non-null")
        }
    }
}


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