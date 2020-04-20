package com.ssquare.myapplication.monokrome.main.data

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class MagazineListLiveData(private val reference: DatabaseReference) : LiveData<List<Magazine>>(),
    ValueEventListener {

    override fun onActive() {
        //add snapshotListener to listenerRegistration
        reference.addValueEventListener(this)
    }

    override fun onInactive() {
        reference.removeEventListener(this)
    }

    override fun onCancelled(p0: DatabaseError) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        //handle databaseError
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        val magazines = dataSnapshot.children.mapNotNull { it.getValue(Magazine::class.java) }
        postValue(magazines)
    }


}