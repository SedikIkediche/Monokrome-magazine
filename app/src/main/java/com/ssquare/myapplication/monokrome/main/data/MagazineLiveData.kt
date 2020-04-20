package com.ssquare.myapplication.monokrome.main.data

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class MagazineLiveData(private val reference: DatabaseReference) : LiveData<Magazine>(),
    ValueEventListener {

    override fun onActive() {
        reference.addValueEventListener(this)
    }

    override fun onInactive() {
        reference.removeEventListener(this)
    }


    override fun onCancelled(p0: DatabaseError) {
        //handleError
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        val magazine = dataSnapshot.getValue(Magazine::class.java)
        postValue(magazine)
    }
}