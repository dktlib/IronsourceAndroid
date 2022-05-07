package com.dktlib.ironsourceutils

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dktlib.ironsourcelib.*

class MainActivity : AppCompatActivity() {
    lateinit var bannerContainer: ViewGroup
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnLoad = findViewById<Button>(R.id.btn_load_inter)
        val btnCallback2 = findViewById<Button>(R.id.btn_show_inter_callback2)
        val btnLoadAndShow = findViewById<Button>(R.id.btn_load_show_inter_callback2)
        val nativeAds = findViewById<LinearLayout>(R.id.nativead)


        val btnReward = findViewById<Button>(R.id.btn_show_reward)
        bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)

//        IronSourceUtil.initIronSource(this, "11726cd45", false)
//        IronSourceUtil.validateIntegration(this)
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

        btnLoadAndShow.setOnClickListener(){

        }

        btnCallback2.setOnClickListener {
            IronSourceUtil.showInterstitialsWithDialogCheckTime(
                this,
                "yo",
                1500,
                0,
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

        btnLoadAndShow.setOnClickListener {
            IronSourceUtil.loadAndShowInterstitialsWithDialogCheckTime(this,"loadandshow",1500,0,object : InterstititialCallback {
                override fun onInterstitialReady() {

                }

                override fun onInterstitialClosed() {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))


                }

                override fun onInterstitialLoadFail() {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))


                }

                override fun onInterstitialShowSucceed() {

                }
            })
        }

        AdmodUtils.getInstance().loadNativeAds(this@MainActivity,
            getString(R.string.test_ads_admob_native_id), nativeAds,
            GoogleENative.UNIFIED_MEDIUM, object : NativeAdCallback {
           override fun onNativeAdLoaded() {}
            override  fun onAdFail() {}
        })


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