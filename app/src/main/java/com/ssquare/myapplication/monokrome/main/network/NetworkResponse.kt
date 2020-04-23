package com.ssquare.myapplication.monokrome.main.network

import androidx.lifecycle.LiveData
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.ssquare.myapplication.monokrome.main.data.Header
import com.ssquare.myapplication.monokrome.main.data.Magazine
import com.ssquare.myapplication.monokrome.main.util.HEADER_PATH
import timber.log.Timber


class NetworkResponse(
    private val databaseReference: DatabaseReference,
    private val storageReference: StorageReference
) :
    LiveData<MagazineListOrException>(),
    ValueEventListener, OnFailureListener {
    private lateinit var headerUrl: String

    init {
        Timber.d("soheib: created NetworkResponse")
        storageReference.child(HEADER_PATH).downloadUrl.addOnSuccessListener {
            headerUrl = it.toString()
        }.addOnFailureListener { exception ->
            Timber.d("soheib: addOnFailureListener() called magazines = $exception")

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
            Timber.d("soheib: onCancelled() called magazines = $exception")
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
            Timber.d("soheib: onDataChange() called magazines = $magazines")
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
            Timber.d("soheib: onFailure() called magazines = $exception")
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