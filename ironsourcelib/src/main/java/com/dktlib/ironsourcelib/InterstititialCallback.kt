package com.dktlib.ironsourcelib

interface InterstititialCallback {
    fun onInterstitialReady()
    fun onInterstitialClosed()
    fun onInterstitialLoadFail()
    fun onInterstitialShowSucceed()
}