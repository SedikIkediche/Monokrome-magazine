package com.ssquare.myapplication.monokrome.main.data

import androidx.lifecycle.LiveData
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

typealias MagazineDataOrException = DataOrException<List<Magazine>, Exception>

class MagazineListLiveData(private val reference: DatabaseReference) :
    LiveData<MagazineDataOrException>(),
    ValueEventListener, OnFailureListener {

    override fun onActive() {
        //add snapshotListener to listenerRegistration
        reference.addValueEventListener(this)
    }

    override fun onInactive() {
        reference.removeEventListener(this)
    }

    override fun onCancelled(error: DatabaseError) {
        val exception = error.toException()
        val dataError = MagazineDataOrException(null, exception)
        postValue(dataError)
    }


    override fun onDataChange(dataSnapshot: DataSnapshot) {
        val magazines = dataSnapshot.children.mapNotNull { it.getValue(Magazine::class.java) }
        val magazineList = MagazineDataOrException(magazines, null)
        postValue(magazineList)
    }

    override fun onFailure(exception: Exception) {
        val magazineException = MagazineDataOrException(null, exception)
        postValue(magazineException)
    }


}