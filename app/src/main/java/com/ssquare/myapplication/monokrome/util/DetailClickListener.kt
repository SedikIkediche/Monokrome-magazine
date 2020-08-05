package com.ssquare.myapplication.monokrome.util

import com.ssquare.myapplication.monokrome.data.Magazine

    interface DetailClickListener {
        fun downloadOrRead(magazine: Magazine)
    }
