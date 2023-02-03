package com.mibitstech.cmsmanag.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mibitstech.cmsmanag.R
import com.mibitstech.cmsmanag.adapter.BoardItemAdapter
import com.mibitstech.cmsmanag.databinding.ActivityMainBinding
import com.mibitstech.cmsmanag.databinding.NavHeaderMainBinding
import com.mibitstech.cmsmanag.firebase.FirestoreClass
import com.mibitstech.cmsmanag.models.Board
import com.mibitstech.cmsmanag.models.User
import com.mibitstech.cmsmanag.utils.Constants

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 22
    }
    private lateinit var mUserName: String
    private var binding: ActivityMainBinding? = null

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpActionBar()

        FirestoreClass().updateUserData(this@MainActivity, true)

        binding?.navView?.setNavigationItemSelectedListener(this)

        binding?.includeAppBarMain?.createBoardFAB?.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)

        }
    }

    private fun setUpActionBar(){
        var toolBarMain = binding?.includeAppBarMain?.toolbarMainActivity
        setSupportActionBar(toolBarMain)
        toolBarMain?.setNavigationIcon(R.drawable.ic_action_nav_main)
        toolBarMain?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            doubleBackExit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().updateUserData(this)
        } else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirestoreClass().getBoardList(this)
        } else {
            Log.e("Cancelled update on Nav", "Cancel")
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.navMyProfile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivityForResult(intent, MY_PROFILE_REQUEST_CODE)
            }

            R.id.navSignOut -> {
                auth = Firebase.auth
                auth.signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User, readBoardList: Boolean){
        val headerView = binding?.navView?.getHeaderView(0)
        val headerBinding = NavHeaderMainBinding.bind(headerView!!)

        mUserName = user.name

        Glide.with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(headerBinding.navHeaderImage)

        headerBinding?.username?.text = user.name

        if(readBoardList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardList(this)
        }

    }

    fun populateBoardListToUi(boardList: ArrayList<Board>){
        hideProgressDialog()
        val mainContentBind = binding?.includeAppBarMain?.root

        if (boardList.size > 0){
            mainContentBind?.findViewById<RecyclerView>(R.id.rv_board_list)?.visibility = View.VISIBLE
            mainContentBind?.findViewById<TextView>(R.id.tv_no_boards_available)?.visibility = View.GONE

            mainContentBind?.findViewById<RecyclerView>(R.id.rv_board_list)?.layoutManager = LinearLayoutManager(this)
            mainContentBind?.findViewById<RecyclerView>(R.id.rv_board_list)?.setHasFixedSize(true)
            val adapter = BoardItemAdapter(this, boardList)
            mainContentBind?.findViewById<RecyclerView>(R.id.rv_board_list)?.adapter = adapter

            //Set the onClickListener for item on RV
            adapter.setOnClickListener(object: BoardItemAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }

            })
        } else {
            mainContentBind?.findViewById<RecyclerView>(R.id.rv_board_list)?.visibility = View.GONE
            mainContentBind?.findViewById<TextView>(R.id.tv_no_boards_available)?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }


}