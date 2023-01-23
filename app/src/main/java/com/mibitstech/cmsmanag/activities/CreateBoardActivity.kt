package com.mibitstech.cmsmanag.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mibitstech.cmsmanag.R
import com.mibitstech.cmsmanag.databinding.ActivityCreateBoardBinding
import com.mibitstech.cmsmanag.firebase.FirestoreClass
import com.mibitstech.cmsmanag.models.Board
import com.mibitstech.cmsmanag.utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateBoardBinding
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        binding.ivBoardImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        //After click on Create btn
        binding.btnCreate.setOnClickListener {
            if(mSelectedImageFileUri != null){
                uploadBoardImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarCreateBoardActivity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.title = resources.getString(R.string.create_board_title)
        }

        binding.toolbarCreateBoardActivity.setNavigationOnClickListener{ onBackPressed()}
    }

    private fun createBoard(){
        val assignToList: ArrayList<String> = ArrayList()
        assignToList.add(getCurrentUserId())

        var board = Board(binding.etBoardName.text.toString(),
            mBoardImageUrl,
            mUserName,
            assignToList
        )

        //Create the board in firestore
        FirestoreClass().createBoard(this, board)
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            } else {
                Toast.makeText(this, "You denied the permission for this app", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mSelectedImageFileUri = data.data!!

            try {
                Glide.with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding.ivBoardImage)

            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    fun createBoardSuccessfully(){
        hideProgressDialog()
        finish()
    }

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef : StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtention(this, mSelectedImageFileUri)
        )
        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
            takeSnapshot ->
            Log.i("Board Image Url: ", takeSnapshot.metadata?.reference?.downloadUrl.toString())
            takeSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                uri ->
                mBoardImageUrl = uri.toString()
                Log.i("Downloadable Image Url:", uri.toString())
                createBoard()
            }
        }.addOnFailureListener {
            e ->
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }

    }
}