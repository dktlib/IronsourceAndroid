package com.dktlib.ironsourceutils

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dktlib.ironsourcelib.InterstititialCallback
import com.dktlib.ironsourcelib.IronSourceLifeCycleHelper
import com.dktlib.ironsourcelib.IronSourceUtil
import com.dktlib.ironsourceutils.databinding.ActivitySplashBinding
import java.util.jar.Manifest

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        IronSourceUtil.initIronSource(this, "85460dcd", true)
        IronSourceUtil.validateIntegration(this)
        this.application.registerActivityLifecycleCallbacks(IronSourceLifeCycleHelper)
        binding.btnNext.setOnClickListener {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        IronSourceUtil.loadInterstitials(this,15000,object : InterstititialCallback {
            override fun onInterstitialReady() {
                binding.btnNext.visibility = View.VISIBLE
                binding.progressBar.visibility = View.INVISIBLE
            }



            override fun onInterstitialClosed() {
                val i = Intent(this@SplashActivity, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(i)
            }

            override fun onInterstitialLoadFail() {
                onInterstitialClosed()
            }

            override fun onInterstitialShowSucceed() {

            }
        })

    }
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                IronSourceUtil.showInterstitialsWithDialogCheckTime(this,"aplssh",1500,0,object : InterstititialCallback {
                    override fun onInterstitialReady() {

                    }

                    override fun onInterstitialClosed() {
                        onInterstitialLoadFail()
                    }

                    override fun onInterstitialLoadFail() {
                        startActivity(Intent(this@SplashActivity,MainActivity::class.java))
                    }

                    override fun onInterstitialShowSucceed() {

                    }
                })
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }
}