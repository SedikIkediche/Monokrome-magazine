package com.ssquare.myapplication.monokrome.main.data

import com.google.firebase.database.FirebaseDatabase

class Repository private constructor(private val database: FirebaseDatabase) {

    companion object {
        var INSTANCE: Repository? = null
        fun getInstance(firebaseDatabase: FirebaseDatabase): Repository {
            var instance = INSTANCE
            if (instance == null) {
                instance = Repository(firebaseDatabase)
                INSTANCE = instance
            }
            return instance
        }
    }

    fun getMagazineList(): MagazineListLiveData {
        //assuming this is the is  the reference related to the list of magazines
        val reference = database.reference.child("magazines")
        return MagazineListLiveData(reference)
    }

    fun getMagazine(id: Long): MagazineLiveData {
        val reference = database.reference.child("magazines").child(id.toString())
        //get MagazineReference by id

        return MagazineLiveData(reference)
    }


}