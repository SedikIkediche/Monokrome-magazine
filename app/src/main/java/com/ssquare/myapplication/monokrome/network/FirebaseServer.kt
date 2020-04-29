package com.ssquare.myapplication.monokrome.network

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.util.HEADER_PATH

class FirebaseServer(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) {

    fun loadFromServer(): Task<MagazineListOrException> {
        val databaseReference = database.reference.child("magazines")
        val storageReference = storage.reference.child(HEADER_PATH)

        val completionTask = TaskCompletionSource<MagazineListOrException>()
        var headerUrl: String? = null

        storageReference.downloadUrl.addOnSuccessListener {
            headerUrl = it.toString()
        }.addOnFailureListener { exception ->
            headerUrl = "Failed Getting Header Url"
            val magazineListOrException = MagazineListOrException(null, null, exception)
            completionTask.setResult(magazineListOrException)
        }

        databaseReference
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    val exception = error.toException()
                    val magazineListOrException =
                        MagazineListOrException(
                            null,
                            null,
                            exception
                        )
                    completionTask.setResult(magazineListOrException)
                }

                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    val magazines =
                        dataSnapShot.children.mapNotNull { it.getValue(NetworkMagazine::class.java) }
                    if (headerUrl != null) {
                        val header = Header(imageUrl = headerUrl!!)
                        val magazineListOrException =
                            MagazineListOrException(
                                magazines,
                                header,
                                null
                            )
                        completionTask.setResult(magazineListOrException)
                    }
                }
            })
        return completionTask.task
    }

}