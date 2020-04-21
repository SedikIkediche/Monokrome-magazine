package com.ssquare.myapplication.monokrome.main.data

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


typealias MagazineOrException = DataOrException<Magazine, Exception>

class MagazineLiveData(private val reference: DatabaseReference) : LiveData<MagazineOrException>(),
    ValueEventListener {

    override fun onActive() {
        reference.addValueEventListener(this)
    }

    override fun onInactive() {
        reference.removeEventListener(this)
    }


    override fun onCancelled(error: DatabaseError) {
        val exception = error.toException()
        val magazineException = MagazineOrException(null, exception)
        postValue(magazineException)
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        val data = dataSnapshot.getValue(Magazine::class.java)
        val magazine = MagazineOrException(data, null)
        postValue(magazine)
    }
}