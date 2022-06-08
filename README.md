# IronSourceUtils
An useful, quick implementation of IronSource Mediation SDK


<!-- GETTING STARTED -->

### Prerequisites

Add this to your project-level build.gradle
  ```sh
  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
Add this to your app-level build.gradle
  ```sh
  dependencies {
	        
//Ironsource
    implementation 'com.github.darkeagle1236:IronSourceUtils:Tag'
    implementation 'com.ironsource.sdk:mediationsdk:7.2.2'

// Add AdMob and Ad Manager Network
    implementation 'com.google.android.gms:play-services-ads:20.6.0'
    implementation 'com.ironsource.adapters:admobadapter:4.3.28'
// Add Facebook Network
    implementation 'com.ironsource.adapters:facebookadapter:4.3.36'
    implementation 'com.facebook.android:audience-network-sdk:6.11.0'

// Add Pangle Network
    implementation 'com.ironsource.adapters:pangleadapter:4.3.13'
    implementation 'com.pangle.global:ads-sdk:4.3.0.9'

	}
  ```
### Usage

#### Init
Add this to onCreate of your first activity
 ```sh
        IronSourceUtil.initIronSource(this, "app-key",true)
 ```
 #### Mediation Adapter
 
 If you're going to use IronSource Mediation with other networks, you have to implement the corresponding network adapter
 Here's all network adapter you need:
 https://developers.is.com/ironsource-mobile/android/mediation-networks-android/#step-1
#### Load interstitial
 ```sh
         IronSourceUtil.loadInterstitials(object : InterstititialCallback {
                override fun onInterstitialReady() {
                    //Interstitial ready, can be able to call IronSourceUtils.showInterstitial()
                }

                override fun onInterstitialClosed() {
                    //Handle user closing the interstitial ads
                }

                override fun onInterstitialLoadFail() {
                    //Interstitial loading failed
                }
            })
 ```
#### Show interstitial
Only available after intersitital loaded successfully
 ```sh		
         IronSourceUtil.showInterstitials(placementId)
 ```
#### Load and show interstitials
 ```sh
         IronSourceUtil.showInterstitialAdsWithCallback(
                this,
                "ad-placement-id",
                true,object : AdCallback {
                    override fun onAdClosed() {
                       // TODO
                    }

                    override fun onAdFail() {
                        // TODO
                    }
                })
        }
 ```
 #### Load a banner
 
 ```sh
 IronSourceUtil.showBanner(activity,viewgroup,"ad-placement-id")
  ```
