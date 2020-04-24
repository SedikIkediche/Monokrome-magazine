package com.ssquare.myapplication.monokrome.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import androidx.preference.PreferenceManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

const val MAGAZINE_PATH = "magazine_path"
const val HEADER_PATH = "header/header.png"
const val DATA_CACHED = "data_cached"
const val DATA_UP_TO_DATE = "data_outdated"
const val PREFERENCE_FILE = "preference_file"

fun isConnected(context: Context): Boolean {
    val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = cm.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

fun commitCacheData(context: Context, isUpToDate: Boolean, isCached: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
        putBoolean(DATA_UP_TO_DATE, isUpToDate)
        if (!isCached) putBoolean(DATA_CACHED, isCached)
        apply()
    }
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
