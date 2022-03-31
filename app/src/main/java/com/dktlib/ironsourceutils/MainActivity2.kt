package com.dktlib.ironsourceutils

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.dktlib.ironsourcelib.InterstititialCallback
import com.dktlib.ironsourcelib.IronSourceUtil

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val btn = findViewById<Button>(R.id.btn_2)
        btn.setOnClickListener {
            IronSourceUtil.showInterstitialsWithDialog(this@MainActivity2,"main",2000,object : InterstititialCallback {
                override fun onInterstitialShowSucceed() {

                }
                override fun onInterstitialReady() {

                }

                override fun onInterstitialClosed() {
                    startActivity(Intent(this@MainActivity2,MainActivity3::class.java))
                }

                override fun onInterstitialLoadFail() {
                    onInterstitialClosed()
                }
            })
        }
    }
    override fun onResume() {
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
        IronSourceUtil.showBanner(this,bannerContainer,"banner_main")
        super.onResume()
    }
}