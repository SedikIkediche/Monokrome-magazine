package com.ssquare.myapplication.monokrome.network

import com.ssquare.myapplication.monokrome.data.Header

data class AuthTokenOrError(
    val authToken: String?,
    val error: Error?
) {
    init {
        if (authToken == null && error == null) {
            throw IllegalArgumentException("Both data and error can't be null")
        } else if (authToken != null && error != null) {
            throw IllegalArgumentException("Both data and error can't be non-null")
        }
    }
}


data class MagazineListOrError(
    val magazineList: List<NetworkMagazine>?,
    val header: Header?,
    val error: Error?
) {
    init {
        if (magazineList == null && header == null && error == null) {
            throw IllegalArgumentException("Both data and error can't be null")
        } else if (magazineList != null && header == null && error != null) {
            throw IllegalArgumentException("Both data and error can't be non-null")
        }
    }
}


data class MagazineOrError(
    val magazine: NetworkMagazine?,
    val error: Error?
) {
    init {
        if (magazine == null && error == null) {
            throw IllegalArgumentException("Both data and error can't be null")
        } else if (magazine != null && error != null) {
            throw IllegalArgumentException("Both data and error can't be non-null")
        }
    }
}