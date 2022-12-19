package com.mibitstech.cmsmanag.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.mibitstech.cmsmanag.R
import com.mibitstech.cmsmanag.databinding.ActivityBaseBinding

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    private var binding: ActivityBaseBinding? = null

    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }

    fun showProgressDialog(text: String){
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)

        mProgressDialog.findViewById<TextView>(R.id.tv_progress_text).text = text

        mProgressDialog.show()
    }

    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    //Function to exit the app when user double press the back button
    fun doubleBackExit(){
        //If user pressed double time
        if (doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }

        //If user pressed for one time
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click once again to exit from app", Toast.LENGTH_LONG).show()
        //If user accidently press the back button and also not double tab on this button then reset the back button functionality after two seconds
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false}, 2000)
    }

    //Display the errors
    fun showErrorSnackbar(message: String){
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))
        snackbar.show()
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}