package com.mibitstech.cmsmanag.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mibitstech.cmsmanag.R
import com.mibitstech.cmsmanag.databinding.ActivityProfileBinding
import com.mibitstech.cmsmanag.firebase.FirestoreClass
import com.mibitstech.cmsmanag.models.User
import com.mibitstech.cmsmanag.utils.Constants
import com.mibitstech.cmsmanag.utils.Constants.PICK_IMAGE_REQUEST_CODE
import com.mibitstech.cmsmanag.utils.Constants.READ_STORAGE_PERMISSION_CODE
import com.mibitstech.cmsmanag.utils.Constants.getFileExtention
import com.mibitstech.cmsmanag.utils.Constants.showImageChooser
import java.io.IOException

class ProfileActivity : BaseActivity() {


    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageUrl: String = ""
    private lateinit var mUserData: User

    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpActionBar()

        FirestoreClass().updateUserData(this)

        binding.profileUserImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_STORAGE_PERMISSION_CODE)
            }
        }

        binding.btnUpdate.setOnClickListener {
            if (mSelectedImageFileUri != null){
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarMyProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
        binding.toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            } else {
                Toast.makeText(this, "You denied the permission for this app", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setUserDateInUi(user: User) {

        mUserData = user

        Glide.with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.profileUserImage)

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if (user.mobile != 0L){
            binding.etMobile.setText(user.mobile.toString())
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mSelectedImageFileUri = data.data!!

            try {
                Glide.with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.profileUserImage)

            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null){
            val sRef : StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtention(this,mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i("Firebase Image Url: ", taskSnapshot.metadata?.reference?.downloadUrl.toString())

                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                    uri ->
                    mProfileImageUrl = uri.toString()
                    Log.i("DownloadableImageUri: ", uri.toString())
                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception ->
                Toast.makeText(this@ProfileActivity, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }

    }

    private fun updateUserProfileData(){

        val userHashMap = HashMap<String, Any>()

        if (mProfileImageUrl.isNotEmpty() && mProfileImageUrl != mUserData.image){
            userHashMap[Constants.IMAGE] = mProfileImageUrl

        }
        if (binding.etName.text.toString() != mUserData.name){
            userHashMap[Constants.NAME] = binding.etName.text.toString()

        }
        if (binding.etMobile.text.toString() != mUserData.mobile.toString()){
            userHashMap[Constants.MOBILE] = binding.etMobile.text.toString().toLong()

        }

        FirestoreClass().updateUserProfileData(this, userHashMap)

    }


    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }


}