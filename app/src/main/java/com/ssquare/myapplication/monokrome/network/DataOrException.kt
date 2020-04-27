package com.ssquare.myapplication.monokrome.network

import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine


data class MagazineListOrException(
    val magazineList: List<Magazine>?,
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