package com.mibitstech.cmsmanag.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mibitstech.cmsmanag.activities.SignInActivity
import com.mibitstech.cmsmanag.activities.SignUpActivity
import com.mibitstech.cmsmanag.models.User
import com.mibitstech.cmsmanag.utils.Constants

class FirestoreClass {

    private lateinit var auth: FirebaseAuth

    private val mFireStore = Firebase.firestore

    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnCompleteListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener {
                e->
                Log.e(activity.javaClass.simpleName, "Error writeing document", e)
            }
    }

    fun signInUser(activity: SignInActivity){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)
                if (loggedInUser != null)
                activity.signInSuccess(loggedInUser)
            }.addOnFailureListener {
                e ->
                Log.e("SignInUser", "Error: ", e)
            }
    }


    //Function to get the userId from the firbase auth user
    fun getCurrentUserId(): String{
        auth = Firebase.auth
        var currentUser = auth.currentUser
        var currentUserID = ""
        if (currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }
}