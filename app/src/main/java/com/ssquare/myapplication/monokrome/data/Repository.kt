package com.ssquare.myapplication.monokrome.data

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

   // init {
    //    database.apply { setPersistenceEnabled(true) }
   // }

    fun getMagazineList(): MagazineListLiveData {
        //assuming this is the is  the reference related to the list of magazines
        val reference = database.reference.child("magazines")
        return MagazineListLiveData(reference)
    }

    fun getMagazine(path: String): MagazineLiveData {
        val reference = database.reference.child(path)
        //get MagazineReference by id

        return MagazineLiveData(reference)
    }


}