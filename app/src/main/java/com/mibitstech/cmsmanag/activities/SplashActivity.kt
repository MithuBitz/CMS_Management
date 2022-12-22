package com.mibitstech.cmsmanag.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.content.ContextCompat.startActivity
import com.mibitstech.cmsmanag.databinding.ActivitySplashBinding
import com.mibitstech.cmsmanag.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {

    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val typeFace: Typeface = Typeface.createFromAsset(assets, "DiplomataSC-Regular.ttf")
        binding?.appNameTv?.typeface = typeFace

//        Handler().postDelayed({
//            startActivity(Intent(this, IntroActivity::class.java))
//        },
//        2500)

        Handler(Looper.getMainLooper()).postDelayed({

            var currentUserId = FirestoreClass().getCurrentUserId()
            if (currentUserId.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }else {
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        },
        2500)

    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}