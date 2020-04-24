package com.ssquare.myapplication.monokrome.network

import androidx.lifecycle.LiveData
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.util.HEADER_PATH


class NetworkResponse(
    private val databaseReference: DatabaseReference,
    private val storageReference: StorageReference
) :
    LiveData<MagazineListOrException>(),
    ValueEventListener, OnFailureListener {
    private lateinit var headerUrl: String

    init {
        storageReference.child(HEADER_PATH).downloadUrl.addOnSuccessListener {
            headerUrl = it.toString()
        }.addOnFailureListener { exception ->
            headerUrl = "Failed Getting Header Url"
            postValue(
                MagazineListOrException(
                    null,
                    null,
                    exception
                )
            )
        }
    }

    override fun onActive() {
        //add snapshotListener to listenerRegistration
        databaseReference.addValueEventListener(this)
    }

    override fun onInactive() {
        databaseReference.removeEventListener(this)
    }

    override fun onCancelled(error: DatabaseError) {
        if (this::headerUrl.isInitialized) {
            val exception = error.toException()
            val dataError =
                MagazineListOrException(
                    null,
                    null,
                    exception
                )
            postValue(dataError)
        }
    }


    override fun onDataChange(dataSnapshot: DataSnapshot) {

        val magazines = dataSnapshot.children.mapNotNull { it.getValue(Magazine::class.java) }
        if (this::headerUrl.isInitialized) {
            val header = Header(imageUrl = headerUrl)
            val magazineList =
                MagazineListOrException(
                    magazines,
                    header,
                    null
                )
            postValue(magazineList)
        }
    }

    override fun onFailure(exception: Exception) {
        if (this::headerUrl.isInitialized) {
            val magazineException =
                MagazineListOrException(
                    null,
                    null,
                    exception
                )
            postValue(magazineException)
        }
    }


}