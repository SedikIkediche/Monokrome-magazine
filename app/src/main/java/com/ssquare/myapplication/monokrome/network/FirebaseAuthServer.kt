package com.ssquare.myapplication.monokrome.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ssquare.myapplication.monokrome.data.User

class FirebaseAuthServer(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase? = null
) {

    val isUserSignedIn = MutableLiveData<Boolean>()

    val isUserCreated = MutableLiveData<Boolean>()

    fun registerUser(email : String, password : String) {

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                database?.reference?.child("Users")?.child(getUserId())
                    ?.setValue(User(email, password))
                isUserCreated.value = true
            } else {
                isUserCreated.value = false
            }
        }
    }

    fun loginUser(email : String, password : String) {

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->

            isUserSignedIn.value = task.isSuccessful
        }
    }

    private fun getUserId() = auth.currentUser!!.uid
}