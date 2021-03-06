package com.dktlib.ironsourcelib

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import com.dktlib.ironsourcelib.utils.SweetAlert.SweetAlertDialog
import com.facebook.shimmer.ShimmerFrameLayout
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.integration.IntegrationHelper
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.model.Placement
import com.ironsource.mediationsdk.sdk.BannerListener
import com.ironsource.mediationsdk.sdk.InterstitialListener
import com.ironsource.mediationsdk.sdk.RewardedVideoListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object IronSourceUtil : LifecycleObserver {
    var enableAds = true
    var isInterstitialAdShowing = false
    lateinit var banner: IronSourceBannerLayout
    var lastTimeInterstitialShowed: Long = 0L
    var lastTimeCallInterstitial:Long = 0L
    var isLoadInterstitialFailed = false
    fun initIronSource(activity: Activity, appKey: String, enableAds: Boolean) {
        this.enableAds = enableAds
        IronSource.init(
            activity,
            appKey,
            IronSource.AD_UNIT.REWARDED_VIDEO,
            IronSource.AD_UNIT.INTERSTITIAL,
            IronSource.AD_UNIT.BANNER
        )
        IronSource.shouldTrackNetworkState(activity,true)
    }

    fun validateIntegration(activity: Activity) {
        IntegrationHelper.validateIntegration(activity);
    }
    fun isInterstitialReady():Boolean{
        return IronSource.isInterstitialReady()
    }
    val TAG: String = "IronSourceUtil"
    fun showInterstitialAdsWithCallback(
        activity: AppCompatActivity,
        adPlacementId: String,
        showLoadingDialog: Boolean,
        callback: AdCallback
    ) {
        IronSource.removeInterstitialListener()
        if (!enableAds) {
            callback.onAdFail()
            return
        }
        var dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
        dialog.getProgressHelper().barColor = Color.parseColor("#A5DC86")
        dialog.setTitleText("Loading ads. Please wait...")
        dialog.setCancelable(false)
        activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
        val mInterstitialListener = object : InterstitialListener {
            override fun onInterstitialAdReady() {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
                    dialog.dismiss()
                }
                Log.d(TAG, activity.lifecycle.currentState.toString())
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    IronSource.showInterstitial(adPlacementId)
                }
                isLoadInterstitialFailed = false
            }

            override fun onInterstitialAdLoadFailed(p0: IronSourceError) {
                if (!activity.isFinishing() && dialog.isShowing()) {
                    dialog.dismiss()
                }
                isLoadInterstitialFailed = true
                callback.onAdFail()
                Log.d(TAG, "onInterstitialAdLoadFailed " + p0.errorMessage)
            }

            override fun onInterstitialAdOpened() {
                Log.d(TAG, "onInterstitialAdOpened")
            }

            override fun onInterstitialAdClosed() {
                callback.onAdClosed()
                isInterstitialAdShowing = false
                loadInterstitials()
                Log.d(TAG, "onInterstitialAdClosed")
            }

            override fun onInterstitialAdShowSucceeded() {
                Log.d(TAG, "onInterstitialAdShowSucceeded")
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onInterstitialAdShowFailed(p0: IronSourceError) {
                if (!activity.isFinishing() && dialog.isShowing()) {
                    dialog.dismiss()
                }
                Log.d(TAG, "onInterstitialAdShowFailed " + p0.errorMessage)
            }

            override fun onInterstitialAdClicked() {
                Log.d(TAG, "onInterstitialAdClicked")
            }
        }
        Log.d(TAG, "isInterstitialNotReady")
        IronSource.removeInterstitialListener()
        IronSource.setInterstitialListener(mInterstitialListener)
        IronSource.loadInterstitial()
        if (showLoadingDialog && (!activity.isFinishing)) {
            dialog.show()
        }
    }



    @Deprecated("Use the new loadInterstitials method with a timeout parameter")
    fun loadInterstitials(callback: InterstititialCallback) {
        if (!enableAds) {
            callback.onInterstitialClosed()
            return
        }
        IronSource.removeInterstitialListener()
        IronSource.setInterstitialListener(object : InterstitialListener {
            override fun onInterstitialAdReady() {
                callback.onInterstitialReady()
                isLoadInterstitialFailed = false
            }

            override fun onInterstitialAdLoadFailed(p0: IronSourceError?) {
                callback.onInterstitialLoadFail(p0?.errorMessage.toString())
                isLoadInterstitialFailed = true
            }

            override fun onInterstitialAdOpened() {

            }

            override fun onInterstitialAdClosed() {
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
                IronSource.loadInterstitial()
            }

            override fun onInterstitialAdShowSucceeded() {
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onInterstitialAdShowFailed(p0: IronSourceError?) {
                callback.onInterstitialClosed()
            }

            override fun onInterstitialAdClicked() {

            }
        })
        if (!IronSource.isInterstitialReady()) {
            IronSource.loadInterstitial()
        }
    }
    //Only use for splash interstitial
    fun loadInterstitials(activity:AppCompatActivity,timeout:Long,callback: InterstititialCallback) {
        if (!enableAds) {
            callback.onInterstitialClosed()
            return
        }
        IronSource.removeInterstitialListener()
        IronSource.setInterstitialListener(object : InterstitialListener {
            override fun onInterstitialAdReady() {
                callback.onInterstitialReady()
                isLoadInterstitialFailed = false
            }

            override fun onInterstitialAdLoadFailed(p0: IronSourceError?) {
                var a = p0!!.errorMessage
                var b = p0!!.errorCode
                var c = 0
                callback.onInterstitialLoadFail(p0?.errorMessage.toString())
                isLoadInterstitialFailed = true
                isInterstitialAdShowing = false
            }

            override fun onInterstitialAdOpened() {

            }

            override fun onInterstitialAdClosed() {
                callback.onInterstitialClosed()
                IronSource.setInterstitialListener(emptyListener)
                isInterstitialAdShowing = false
                IronSource.loadInterstitial()
            }

            override fun onInterstitialAdShowSucceeded() {
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onInterstitialAdShowFailed(p0: IronSourceError?) {
                callback.onInterstitialClosed()
            }

            override fun onInterstitialAdClicked() {

            }
        })
        if (!IronSource.isInterstitialReady()) {
            IronSource.loadInterstitial()
            activity.lifecycleScope.launch(Dispatchers.Main) {
                delay(timeout)
                if((!IronSource.isInterstitialReady())&&(!isInterstitialAdShowing)){
                    callback.onInterstitialLoadFail("!IronSource.isInterstitialReady()")
                }
            }
        }
        else{
            callback.onInterstitialReady()
        }
    }
    @MainThread
    fun loadInterstitials() {
        if (!enableAds) {
            return
        }
        IronSource.setInterstitialListener(emptyListener)
        if(!IronSource.isInterstitialReady()){
            IronSource.loadInterstitial()
        }
    }

//    @Deprecated("Use the new showInterstitialsWithDialog method")
//    fun showInterstitials(placementId: String) {
//        //Throttle calling interstitial
//        if(System.currentTimeMillis() - 1000 < lastTimeCallInterstitial){
//            return
//        }
//        lastTimeCallInterstitial = System.currentTimeMillis()
//        if (IronSource.isInterstitialReady()) {
//            IronSource.showInterstitial(placementId)
//        }
//    }

    val emptyListener = object : InterstitialListener {
        override fun onInterstitialAdReady() {
            Log.d(TAG, "onInterstitialAdReady")
            isLoadInterstitialFailed = false
        }

        override fun onInterstitialAdLoadFailed(p0: IronSourceError?) {
            Log.d(TAG, "onInterstitialAdLoadFailed")
            isLoadInterstitialFailed = true
            isInterstitialAdShowing = false
        }

        override fun onInterstitialAdOpened() {
            Log.d(TAG, "onInterstitialAdOpened")
        }

        override fun onInterstitialAdClosed() {
            Log.d(TAG, "onInterstitialAdClosed")
            isInterstitialAdShowing = false
        }

        override fun onInterstitialAdShowSucceeded() {
            Log.d(TAG, "onInterstitialAdShowSucceeded")
            isInterstitialAdShowing = true
        }

        override fun onInterstitialAdShowFailed(p0: IronSourceError?) {
            Log.d(TAG, "onInterstitialAdShowFailed")
        }

        override fun onInterstitialAdClicked() {
            Log.d(TAG, "onInterstitialAdClicked")
        }
    }

//    @MainThread
//    fun showInterstitialsWithDialog(
//        activity: AppCompatActivity,
//        placementId: String,
//        dialogShowTime: Long,
//        callback: InterstititialCallback
//    ) {
//        //Throttle calling interstitial
//        if(System.currentTimeMillis() - 1000 < lastTimeCallInterstitial){
//            return
//        }
//        lastTimeCallInterstitial = System.currentTimeMillis()
//        if (!enableAds) {
//            callback.onInterstitialLoadFail()
//            return
//        }
//        IronSource.setInterstitialListener(object : InterstitialListener {
//            override fun onInterstitialAdReady() {
//                activity.lifecycleScope.launch(Dispatchers.Main) {
//                    isLoadInterstitialFailed = false
//                    callback.onInterstitialReady()
//                    IronSource.setInterstitialListener(emptyListener)
//                }
//            }
//
//            override fun onInterstitialAdLoadFailed(p0: IronSourceError?) {
//                activity.lifecycleScope.launch(Dispatchers.Main) {
//                    isLoadInterstitialFailed = true
//                    callback.onInterstitialLoadFail()
//                    IronSource.setInterstitialListener(emptyListener)
//                }
//            }
//
//            override fun onInterstitialAdOpened() {
//
//            }
//
//            override fun onInterstitialAdClosed() {
//                callback.onInterstitialClosed()
//                isInterstitialAdShowing = false
//                loadInterstitials()
//            }
//
//            override fun onInterstitialAdShowSucceeded() {
//                callback.onInterstitialShowSucceed()
//                lastTimeInterstitialShowed = System.currentTimeMillis()
//                isInterstitialAdShowing = true
//            }
//
//            override fun onInterstitialAdShowFailed(p0: IronSourceError?) {
//                callback.onInterstitialClosed()
//            }
//
//            override fun onInterstitialAdClicked() {
//
//            }
//        })
//
//        if (IronSource.isInterstitialReady()) {
//            activity.lifecycleScope.launch {
//                if (dialogShowTime > 0) {
//                    var dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
//                    dialog.getProgressHelper().barColor = Color.parseColor("#A5DC86")
//                    dialog.setTitleText("Loading ads. Please wait...")
//                    dialog.setCancelable(false)
//                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
//                    if (!activity.isFinishing) {
//                        dialog.show()
//                    }
//                    delay(dialogShowTime)
//                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
//                        dialog.dismiss()
//                    }
//                }
//                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
//                    Log.d(TAG, "onInterstitialAdReady")
//                    IronSource.showInterstitial(placementId)
//                }
//            }
//        } else {
//            activity.lifecycleScope.launch(Dispatchers.Main) {
//                IronSource.setInterstitialListener(emptyListener)
//                callback.onInterstitialClosed()
//                isInterstitialAdShowing = false
//                isLoadInterstitialFailed = true
//            }
//        }
//    }
    @MainThread
    fun showInterstitialsWithDialogCheckTime(
        activity: AppCompatActivity,
        placementId: String,
        dialogShowTime: Long,
        timeInMillis: Long,
        callback: InterstititialCallback
    ) {

    if (AppOpenManager.getInstance().isInitialized) {
        if (!AppOpenManager.getInstance().isAppResumeEnabled) {
            return
        } else {
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = false
            }
        }
    }

        if(System.currentTimeMillis() - 1000 < lastTimeCallInterstitial){
            return
        }
        lastTimeCallInterstitial = System.currentTimeMillis()
        if (!enableAds) {
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            callback.onInterstitialLoadFail("\"isNetworkConnected\"")
            return
        }
        if (!(System.currentTimeMillis() - timeInMillis > lastTimeInterstitialShowed) || (!enableAds)) {
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            callback.onInterstitialLoadFail("\"isNetworkConnected\"")
            return
        }
        IronSource.setInterstitialListener(object : InterstitialListener {
            override fun onInterstitialAdReady() {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    isLoadInterstitialFailed = false

                    callback.onInterstitialReady()
                    IronSource.setInterstitialListener(emptyListener)
                }
            }

            override fun onInterstitialAdLoadFailed(p0: IronSourceError?) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    isLoadInterstitialFailed = true
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                    }
                    callback.onInterstitialLoadFail(p0?.errorMessage.toString())
                    IronSource.setInterstitialListener(emptyListener)
                }
            }

            override fun onInterstitialAdOpened() {

            }

            override fun onInterstitialAdClosed() {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
                loadInterstitials()
            }

            override fun onInterstitialAdShowSucceeded() {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onInterstitialAdShowFailed(p0: IronSourceError?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
            }

            override fun onInterstitialAdClicked() {

            }
        })

        if (IronSource.isInterstitialReady()) {
            activity.lifecycleScope.launch {
                if (dialogShowTime > 0) {
                    var dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
                    dialog.getProgressHelper().barColor = Color.parseColor("#A5DC86")
                    dialog.setTitleText("Loading ads. Please wait...")
                    dialog.setCancelable(false)
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
                    if (!activity.isFinishing) {
                        dialog.show()
                    }
                    delay(dialogShowTime)
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
                        dialog.dismiss()
                    }
                }
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    IronSource.showInterstitial(placementId)
                }
            }
        } else {
            activity.lifecycleScope.launch(Dispatchers.Main) {
                IronSource.setInterstitialListener(emptyListener)
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
                isLoadInterstitialFailed = true
            }
        }
    }

    @MainThread
    fun loadAndShowInterstitialsWithDialogCheckTime(
        activity: AppCompatActivity,
        placementId: String,
        dialogShowTime: Long,
        timeout: Long,
        callback: InterstititialCallback
    ) {
        var dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
        dialog.getProgressHelper().barColor = Color.parseColor("#A5DC86")
        dialog.setTitleText("Loading ads. Please wait...")
        dialog.setCancelable(false)

        if(!IronSource.isInterstitialReady()){
            IronSource.loadInterstitial()
//            activity.lifecycleScope.launch(Dispatchers.Main) {
//                var timeout2 = timeout.toInt();
//                if (timeout2 <= 0) {
//                    timeout2 = 30000
//                }
//                delay(timeout2.toLong())
//                if((!IronSource.isInterstitialReady())&&(isInterstitialAdShowing)){
//                    if (AppOpenManager.getInstance().isInitialized) {
//                        AppOpenManager.getInstance().isAppResumeEnabled = true
//                        Log.e("isAppResumeEnabled", "1" + AppOpenManager.getInstance().isAppResumeEnabled)
//                    }
//                    callback.onInterstitialLoadFail()
//                    IronSource.setInterstitialListener(emptyListener)
//                }
//            }
        }

        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                    Log.e("isAppResumeEnabled", "2" + AppOpenManager.getInstance().isAppResumeEnabled)

                }
            }
        }

        lastTimeCallInterstitial = System.currentTimeMillis()
        if (!enableAds||!isNetworkConnected(activity)) {
            Log.e("isNetworkConnected", "1" + AppOpenManager.getInstance().isAppResumeEnabled)
            if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
                dialog.dismiss()
            }
            Log.e("isNetworkConnected", "2" + AppOpenManager.getInstance().isAppResumeEnabled)

            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
                Log.e("isNetworkConnected", "3" + AppOpenManager.getInstance().isAppResumeEnabled)

                Log.e("isAppResumeEnabled", "3" + AppOpenManager.getInstance().isAppResumeEnabled)
            }
            Log.e("isNetworkConnected", "4" + AppOpenManager.getInstance().isAppResumeEnabled)

            isInterstitialAdShowing = false
            Log.e("isNetworkConnected", "5" + AppOpenManager.getInstance().isAppResumeEnabled)

            callback.onInterstitialLoadFail("isNetworkConnected")
            IronSource.setInterstitialListener(emptyListener)
            return
        }

        IronSource.setInterstitialListener(object : InterstitialListener {
            override fun onInterstitialAdReady() {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
                    dialog.dismiss()
                }
//                if (AppOpenManager.getInstance().isInitialized) {
//                    if (!AppOpenManager.getInstance().isAppResumeEnabled) {
//                        return
//                    }
//                }
                IronSource.showInterstitial(placementId)
            }

            override fun onInterstitialAdLoadFailed(p0: IronSourceError?) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
                        dialog.dismiss()
                    }
                    isLoadInterstitialFailed = true
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                        Log.e("isAppResumeEnabled", "4" + AppOpenManager.getInstance().isAppResumeEnabled)

                    }
                    isInterstitialAdShowing = false
                    callback.onInterstitialLoadFail(p0?.errorMessage.toString())
                    IronSource.setInterstitialListener(emptyListener)
                }
            }

            override fun onInterstitialAdOpened() {

            }

            override fun onInterstitialAdClosed() {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
                    dialog.dismiss()
                }
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                    Log.e("isAppResumeEnabled", "5" + AppOpenManager.getInstance().isAppResumeEnabled)

                }
                isInterstitialAdShowing = false

                callback.onInterstitialClosed()
                IronSource.setInterstitialListener(emptyListener)

                loadInterstitials()
            }

            override fun onInterstitialAdShowSucceeded() {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
                    dialog.dismiss()
                }
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                    Log.e("isAppResumeEnabled", "6" + AppOpenManager.getInstance().isAppResumeEnabled)

                }
                callback.onInterstitialShowSucceed()

                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onInterstitialAdShowFailed(p0: IronSourceError?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                    Log.e("isAppResumeEnabled", "7" + AppOpenManager.getInstance().isAppResumeEnabled)

                }
                isInterstitialAdShowing = false
                callback.onInterstitialClosed()
                IronSource.setInterstitialListener(emptyListener)

                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
                    dialog.dismiss()
                }
            }

            override fun onInterstitialAdClicked() {

            }
        })

        if (IronSource.isInterstitialReady()) {
            activity.lifecycleScope.launch {
                if (dialogShowTime > 0) {
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
                    if (!activity.isFinishing) {
                        dialog.show()
                    }
                    delay(dialogShowTime)
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
                        dialog.dismiss()
                    }
                }
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    IronSource.showInterstitial(placementId)
                }
            }
        } else {
            if (dialogShowTime > 0) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
                    if (!activity.isFinishing) {
                        dialog.show()
                    }
                }
            }

        }
    }

//    fun showInterstitialAdsWithCallbackCheckTime(
//        activity: AppCompatActivity,
//        adPlacementId: String,
//        showLoadingDialog: Boolean,
//        timeInMillis: Long,
//        callback: AdCallback
//    ) {
//        if (!enableAds) {
//            callback.onAdFail()
//            return
//        }
//        IronSource.removeInterstitialListener()
//        if (!(System.currentTimeMillis() - timeInMillis > lastTimeInterstitialShowed) || (!enableAds)) {
//            callback.onAdFail()
//            return
//        }
//        var dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
//        dialog.getProgressHelper().barColor = Color.parseColor("#A5DC86")
//        dialog.setTitleText("Loading ads. Please wait...")
//        dialog.setCancelable(false)
//        activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
//        val mInterstitialListener = object : InterstitialListener {
//            override fun onInterstitialAdReady() {
//                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing()) {
//                    dialog.dismiss()
//                }
//                Log.d(TAG, activity.lifecycle.currentState.toString())
//                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
//                    Log.d(TAG, "onInterstitialAdReady")
//                    IronSource.showInterstitial(adPlacementId)
//                }
//            }
//
//            override fun onInterstitialAdLoadFailed(p0: IronSourceError) {
//                if (!activity.isFinishing() && dialog.isShowing()) {
//                    dialog.dismiss()
//                }
//                callback.onAdFail()
//                Log.d(TAG, "onInterstitialAdLoadFailed " + p0.errorMessage)
//            }
//
//            override fun onInterstitialAdOpened() {
//                Log.d(TAG, "onInterstitialAdOpened")
//            }
//
//            override fun onInterstitialAdClosed() {
//                callback.onAdClosed()
//                isInterstitialAdShowing = false
//                Log.d(TAG, "onInterstitialAdClosed")
//            }
//
//            override fun onInterstitialAdShowSucceeded() {
//                Log.d(TAG, "onInterstitialAdShowSucceeded")
//                lastTimeInterstitialShowed = System.currentTimeMillis()
//                isInterstitialAdShowing = true
//            }
//
//            override fun onInterstitialAdShowFailed(p0: IronSourceError) {
//                if (!activity.isFinishing() && dialog.isShowing()) {
//                    dialog.dismiss()
//                }
//                Log.d(TAG, "onInterstitialAdShowFailed " + p0.errorMessage)
//            }
//
//            override fun onInterstitialAdClicked() {
//                Log.d(TAG, "onInterstitialAdClicked")
//            }
//        }
//        Log.d(TAG, "isInterstitialNotReady")
//        IronSource.loadInterstitial()
//        if (showLoadingDialog && (!activity.isFinishing)) {
//            dialog.show()
//        }
//        IronSource.setInterstitialListener(mInterstitialListener);
//    }

    fun showBanner(activity: AppCompatActivity, bannerContainer: ViewGroup, adPlacementId: String) {
        if (!this.enableAds) {
            bannerContainer.visibility = View.GONE
            return
        }
        destroyBanner()
        bannerContainer.removeAllViews()
        banner = IronSource.createBanner(activity, ISBannerSize.SMART)
        val tagView: View =
            activity.getLayoutInflater().inflate(R.layout.banner_shimmer_layout, null, false)
        bannerContainer.addView(tagView, 0)
        bannerContainer.addView(banner,1)
        val shimmerFrameLayout: ShimmerFrameLayout =
            tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmerAnimation()
        banner.bannerListener = object : BannerListener {
            override fun onBannerAdLoaded() {
                shimmerFrameLayout.stopShimmerAnimation()
                bannerContainer.removeView(tagView)
            }

            override fun onBannerAdLoadFailed(p0: IronSourceError?) {
                bannerContainer.removeAllViews()
            }

            override fun onBannerAdClicked() {
                Log.d(TAG, "onBannerAdClicked")
            }

            override fun onBannerAdScreenPresented() {
                Log.d(TAG, "onBannerAdScreenPresented")
            }

            override fun onBannerAdScreenDismissed() {
                Log.d(TAG, "onBannerAdScreenDismissed")
            }

            override fun onBannerAdLeftApplication() {
                Log.d(TAG, "onBannerAdLeftApplication")
            }
        }
        IronSource.loadBanner(banner, adPlacementId)
//        activity.lifecycle.addObserver(object:LifecycleObserver{
//            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//            fun onPause(){
//                destroyBanner(banner,bannerContainer)
//                activity.lifecycle.removeObserver(this)
//            }
//        })
    }

    fun destroyBanner() {
//        viewGroup.removeAllViews()
        if (this::banner.isInitialized) {
            IronSource.destroyBanner(banner)
        }
    }

    fun loadAndShowRewardsAds(placementId: String,callback: RewardVideoCallback){
        IronSource.setRewardedVideoListener(object : RewardedVideoListener {
            override fun onRewardedVideoAdOpened() {

            }

            override fun onRewardedVideoAdClosed() {
                callback.onRewardClosed()
            }

            override fun onRewardedVideoAvailabilityChanged(p0: Boolean) {

            }

            override fun onRewardedVideoAdStarted() {

            }

            override fun onRewardedVideoAdEnded() {

            }

            override fun onRewardedVideoAdRewarded(p0: Placement?) {
                callback.onRewardEarned()
            }

            override fun onRewardedVideoAdShowFailed(p0: IronSourceError?) {
                callback.onRewardFailed()
            }

            override fun onRewardedVideoAdClicked(p0: Placement?) {

            }
        })
        if (IronSource.isRewardedVideoAvailable()){
            IronSource.showRewardedVideo(placementId)
        }
        else{
            callback.onRewardNotAvailable()
        }
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var vau = cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        Log.e("isNetworkConnected", "0" + vau)
        return vau
    }
}