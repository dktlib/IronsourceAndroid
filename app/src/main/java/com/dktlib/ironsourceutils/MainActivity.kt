package com.dktlib.ironsourceutils

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dktlib.ironsourcelib.*

class MainActivity : AppCompatActivity() {
    lateinit var bannerContainer: ViewGroup
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnLoad = findViewById<Button>(R.id.btn_load_inter)
        val btnShow = findViewById<Button>(R.id.btn_show_inter)
        val btnCallback2 = findViewById<Button>(R.id.btn_show_inter_callback2)
        val btnReward = findViewById<Button>(R.id.btn_show_reward)
        bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)

        IronSourceUtil.initIronSource(this, "85460dcd", false)
        IronSourceUtil.validateIntegration(this)
        btnLoad.setOnClickListener {
//            IronSourceUtil.showInterstitialAdsWithCallback(this@MainActivity,"main",true,object : AdCallback {
//                override fun onAdClosed() {
//                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
//                }
//
//                override fun onAdFail() {
//                    onAdClosed()
//                }
//            })
            IronSourceUtil.loadInterstitials()

        }
        btnShow.setOnClickListener {
            IronSourceUtil.showInterstitialsWithDialog(
                this,
                "yo",
                1500,
                object : InterstititialCallback {
                    override fun onInterstitialShowSucceed() {

                    }

                    override fun onInterstitialReady() {

                    }

                    override fun onInterstitialClosed() {
                        startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                    }

                    override fun onInterstitialLoadFail() {
                        onInterstitialClosed()
                    }
                })
        }
        btnCallback2.setOnClickListener {
            IronSourceUtil.showInterstitialsWithDialog(
                this,
                "yo",
                1500,
                object : InterstititialCallback {
                    override fun onInterstitialShowSucceed() {

                    }

                    override fun onInterstitialReady() {

                    }

                    override fun onInterstitialClosed() {
                        startActivity(Intent(this@MainActivity, MainActivity3::class.java))
                    }

                    override fun onInterstitialLoadFail() {
                        onInterstitialClosed()
                    }
                })
        }
        btnReward.setOnClickListener {
            IronSourceUtil.loadAndShowRewardsAds("rewards",object : RewardVideoCallback {
                override fun onRewardClosed() {

                }

                override fun onRewardEarned() {

                }

                override fun onRewardFailed() {

                }

                override fun onRewardNotAvailable() {

                }
            })
        }
    }

    //    override fun onPause() {
//        if(this::bannerContainer.isInitialized){
//            IronSourceUtil.destroyBanner(bannerContainer)
//        }
//        super.onPause()
//    }
    override fun onResume() {
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
        IronSourceUtil.showBanner(this, bannerContainer, "main")
        super.onResume()
    }
}