package com.ssquare.myapplication.monokrome.network

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class FirebaseServer(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) {

    fun loadData(): NetworkResponse {
        val databaseReference = database.reference.child("magazines")
        val storageReference = storage.reference

        return NetworkResponse(databaseReference, storageReference)
    }

}