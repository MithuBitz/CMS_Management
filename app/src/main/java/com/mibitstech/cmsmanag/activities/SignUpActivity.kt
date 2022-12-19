package com.mibitstech.cmsmanag.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mibitstech.cmsmanag.R
import com.mibitstech.cmsmanag.databinding.ActivitySignUpBinding

class SignUpActivity : BaseActivity() {
    private var binding: ActivitySignUpBinding? = null

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        //FullScreen display
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setUpActionBar()

        binding?.btnSignUp?.setOnClickListener {
            registerUser()
        }
    }

    //Fuction that handle to set the action bar
    private fun setUpActionBar(){
        setSupportActionBar(binding?.toolbarSignUpActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
        }
        binding?.toolbarSignUpActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean{
        return  when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackbar("Please Enter a name")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackbar("Please enter email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackbar("Please enter a password")
                false
            } else -> {
                true
            }
        }
    }

    private fun registerUser(){
        val name: String = binding?.etName?.text.toString().trim{ it <= ' '}
        val email: String = binding?.etEmail?.text.toString().trim{ it <= ' '}
        val password: String = binding?.etPassword?.text.toString().trim{ it <= ' '}

        if (validateForm(name, email, password)){
            //Toast.makeText(this@SignUpActivity, "Now you can register a new user", Toast.LENGTH_LONG).show()
            //Todo show the progress bar
            showProgressDialog(resources.getString(R.string.please_wait))
            auth = Firebase.auth
            //Create a firebase auth with get instance and create a user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                //and link addOnCompleteListener to response with the firebase console
                .addOnCompleteListener { task ->
                    //on the besis of the listener if task is responded hide the progress bar
                    hideProgressDialog()
                    //then if task.isSuccessful
                    if (task.isSuccessful) {
                        //then create a firebase user object from the task.result.user
                        val firebaseUser: FirebaseUser = task!!.result!!.user!!
                        //and also get the registered email from the firebase user email
                        val registerdEmail = firebaseUser.email!!
                        //Create a toast to show the name of the user and email
                        Toast.makeText(this@SignUpActivity, "$name create a user with email address $registerdEmail", Toast.LENGTH_SHORT).show()
                        auth.signOut()
                        finish()
                    } else {
                        Toast.makeText(this@SignUpActivity, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }


        }
    }
}