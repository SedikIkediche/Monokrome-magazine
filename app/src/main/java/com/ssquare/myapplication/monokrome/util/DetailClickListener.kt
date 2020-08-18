package com.ssquare.myapplication.monokrome.util

import com.ssquare.myapplication.monokrome.data.DomainMagazine

interface DetailClickListener {
    fun downloadOrRead(magazine: DomainMagazine)
}
