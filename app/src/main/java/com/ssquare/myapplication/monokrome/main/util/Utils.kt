package com.ssquare.myapplication.monokrome.main.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

const val MAGAZINE_PATH = "magazine_path"
const val HEADER_PATH = "header/header.png"


fun isConnected(context: Context): Boolean {
    val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = cm.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

fun FirebaseDatabase.networkOrCache(error: () -> Unit): DataSourceType {
    var type = DataSourceType.NETWORK
    this.getReference(".info/connected").addValueEventListener(object : ValueEventListener {

        override fun onDataChange(dataSource: DataSnapshot) {
            val connected = dataSource.getValue(Boolean::class.java) ?: false
            type = if (connected) {
                DataSourceType.NETWORK
            } else {
                DataSourceType.CACHE
            }
        }

        override fun onCancelled(p0: DatabaseError) {
            error()
            type = DataSourceType.UNKNOWN
        }
    })
    return type
}