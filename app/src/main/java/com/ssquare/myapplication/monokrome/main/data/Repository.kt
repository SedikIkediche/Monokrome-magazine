package com.ssquare.myapplication.monokrome.main.data

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class Repository private constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) {

    companion object {
        var INSTANCE: Repository? = null
        fun getInstance(
            firebaseDatabase: FirebaseDatabase,
            firebaseStorage: FirebaseStorage
        ): Repository {
            var instance = INSTANCE
            if (instance == null) {
                instance = Repository(firebaseDatabase, firebaseStorage)
                INSTANCE = instance
            }
            return instance
        }
    }

    fun getMagazineList(): MagazineListLiveData {
        //assuming this is the is  the reference related to the list of magazines
        val databaseReference = database.reference.child("magazines")
        val storageReference = storage.reference
        return MagazineListLiveData(databaseReference, storageReference)
    }

    fun getMagazine(path: String): MagazineLiveData {
        val reference = database.reference.child(path)

        return MagazineLiveData(reference)
    }


}