package com.ssquare.myapplication.monokrome.main.data

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.*

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

    override fun onDataChange(p0: DataSnapshot) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        //retrieve data as dataModel
        // setValue(dataModel) or PostValue  i guess
    }


}